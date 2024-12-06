package io.github.guttenbase.schema.comparison

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.ColumnDataMappingTool

/**
 * Will check two schemas for compatibility and report found issues.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [TableOrderHint] to determine order of tables
 */
@Suppress("MemberVisibilityCanBePrivate")
class SchemaComparatorTool(val connectorRepository: ConnectorRepository) {
  private val schemaCompatibilityIssues = SchemaCompatibilityIssues()

  /**
   * Check compatibility of both connectors/schemata.
   *
   * @param sourceConnectorId
   * @param targetConnectorId
   * @return List of found issues. If empty the schemas are completely compatible
   */
  fun check(sourceConnectorId: String, targetConnectorId: String): SchemaCompatibilityIssues {
    val sourceTables: List<TableMetaData> = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val tableMapper: TableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val targetDatabase = connectorRepository.getDatabaseMetaData(targetConnectorId)

    checkEqualTables(sourceTables, targetDatabase, tableMapper)

    for (sourceTable in sourceTables) {
      val targetTable = tableMapper.map(sourceTable, targetDatabase)

      if (targetTable != null) {
        checkEqualColumns(sourceConnectorId, targetConnectorId, sourceTable, targetTable)
        checkEqualForeignKeys(sourceTable, targetTable)
        checkEqualIndexes(sourceTable, targetTable)
        checkDuplicateIndexes(sourceTable)
        checkForeignKeys(sourceTable)
        checkDuplicateIndexes(targetTable)
        checkForeignKeys(targetTable)
      }
    }

    return schemaCompatibilityIssues
  }

  fun checkEqualForeignKeys(sourceTable: TableMetaData, targetTable: TableMetaData): SchemaCompatibilityIssues {
    for (sourceFK in sourceTable.importedForeignKeys) {
      targetTable.importedForeignKeys.firstOrNull {
        sourceFK.referencedColumns == it.referencedColumns &&
            sourceFK.tableMetaData == it.tableMetaData &&
            sourceFK.referencingColumns == it.referencingColumns
      } ?: schemaCompatibilityIssues.addIssue(
        MissingForeignKeyIssue(
          "Missing/incompatible foreign key $sourceFK",
          sourceFK
        )
      )
    }

    return schemaCompatibilityIssues
  }

  fun checkEqualIndexes(sourceTable: TableMetaData, targetTable: TableMetaData): SchemaCompatibilityIssues {
    for (sourceIndex in sourceTable.indexes) {
      targetTable.indexes.firstOrNull {
        sourceIndex.columnMetaData == it.columnMetaData
      } ?: schemaCompatibilityIssues.addIssue(MissingIndexIssue("Missing index $sourceIndex", sourceIndex))
    }

    return schemaCompatibilityIssues
  }

  fun checkDuplicateIndexes(table: TableMetaData): SchemaCompatibilityIssues {
    val indexMetaDataMap = LinkedHashMap<String, IndexMetaData>()

    for (index in table.indexes) {
      val keyColumns = index.columnMetaData.sorted().joinToString { it.columnName }

      if (indexMetaDataMap.containsKey(keyColumns)) {
        val conflictingIndex = indexMetaDataMap[keyColumns]

        schemaCompatibilityIssues.addIssue(
          DuplicateIndexIssue("Duplicate index " + conflictingIndex + "vs." + index, index)
        )
      } else {
        indexMetaDataMap[keyColumns] = index
      }
    }

    return schemaCompatibilityIssues
  }

  fun checkForeignKeys(table: TableMetaData): SchemaCompatibilityIssues {
    val fkMetaDataMap = LinkedHashMap<String, ForeignKeyMetaData>()

    for (foreignKey in table.exportedForeignKeys) {
      val keyColumns = getFullyQualifiedColumnNames(foreignKey.referencingColumns) + ":" +
          getFullyQualifiedColumnNames(foreignKey.referencingColumns)

      if (fkMetaDataMap.containsKey(keyColumns)) {
        val conflictingKey = fkMetaDataMap[keyColumns]

        schemaCompatibilityIssues.addIssue(
          DuplicateForeignKeyIssue("Duplicate foreignKey " + conflictingKey + "vs." + foreignKey, foreignKey)
        )
      } else {
        fkMetaDataMap[keyColumns] = foreignKey
      }
    }

    return schemaCompatibilityIssues
  }

  fun checkEqualColumns(
    sourceConnectorId: String, targetConnectorId: String,
    sourceTableMetaData: TableMetaData, targetTableMetaData: TableMetaData
  ): SchemaCompatibilityIssues {
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val sourceColumnNameMapper = connectorRepository.hint<ColumnMapper>(sourceConnectorId)
    val targetColumnNameMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val tableName = sourceTableMetaData.tableName
    val sourceColumns = ColumnOrderHint.getSortedColumns(connectorRepository, sourceConnectorId, sourceTableMetaData)
    val mappedTargetColumns = HashSet<ColumnMetaData>(targetTableMetaData.columnMetaData)

    for (sourceColumn in sourceColumns) {
      val mapping = columnMapper.map(sourceColumn, targetTableMetaData)
      val targetColumns = mapping.columns
      val sourceColumnName = sourceColumnNameMapper.mapColumnName(sourceColumn, sourceTableMetaData)

      if (targetColumns.isEmpty()) {
        if (mapping.isEmptyColumnListOk) {
          schemaCompatibilityIssues.addIssue(
            DroppedColumnIssue(
              "No mapping column(s) found for: $tableName:$sourceColumn -> Will be dropped",
              sourceColumn
            )
          )
        } else {
          schemaCompatibilityIssues.addIssue(
            MissingColumnIssue(
              "No mapping column(s) found for: $tableName:$sourceColumn",
              sourceColumn
            )
          )
        }
      }

      mappedTargetColumns.removeAll(targetColumns.toSet())

      for (targetColumn in targetColumns) {
        val targetColumnName = targetColumnNameMapper.mapColumnName(targetColumn, targetTableMetaData)
        val columnTypeMapping = ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(
          sourceColumn, targetConnectorId, targetColumn
        )

        if (columnTypeMapping == null) {
          schemaCompatibilityIssues.addIssue(
            IncompatibleColumnsIssue(
              """
        $tableName: $sourceColumn: Columns have incompatible types: 
        $sourceColumnName/${sourceColumn.columnTypeName}/${sourceColumn.columnClassName} vs.
        $targetColumnName/${targetColumn.columnTypeName}/${targetColumn.columnClassName}            
              """.trimIndent(), sourceColumn, targetColumn
            )
          )
        }
      }
    }

    for (targetColumn in mappedTargetColumns) {
      if (targetColumn.isNullable) {
        schemaCompatibilityIssues.addIssue(
          AdditionalColumnIssue("Unmapped target column (Will be null): $tableName:$targetColumn", targetColumn)
        )
      } else {
        schemaCompatibilityIssues.addIssue(
          AdditionalNonNullColumnIssue(
            "Unmapped target column with not-null constraint will cause error : $tableName:$targetColumn",
            targetColumn
          )
        )
      }
    }

    return schemaCompatibilityIssues
  }

  private fun checkEqualTables(
    sourceTableMetaData: List<TableMetaData>, targetDatabaseMetaData: DatabaseMetaData, tableMapper: TableMapper
  ) {
    for (tableMetaData in sourceTableMetaData) {
      val targetTableMetaData = tableMapper.map(tableMetaData, targetDatabaseMetaData)

      if (targetTableMetaData == null) {
        schemaCompatibilityIssues.addIssue(
          MissingTableIssue("Table $tableMetaData is unknown/unmapped in target schema", tableMetaData)
        )
      }
    }
  }

  companion object {
    private fun getFullyQualifiedColumnNames(columnMetaData: List<ColumnMetaData>) =
      columnMetaData.joinToString(
        separator = ", ", prefix = "(", postfix = ")",
        transform = { it.tableMetaData.tableName + "." + it.columnName })
  }
}
