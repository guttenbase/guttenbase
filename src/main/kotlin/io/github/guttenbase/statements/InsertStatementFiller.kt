package io.github.guttenbase.statements

import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.MissingDataException
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.ColumnOrderHint.Companion.getSortedColumns
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableRowDataFilter
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.connectorId
import io.github.guttenbase.progress.TableCopyProgressIndicator
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.ColumnDataMappingTool
import io.github.guttenbase.tools.ColumnMapping
import java.io.Closeable
import java.io.IOException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Fill previously created INSERT statement with data from source connector.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [ColumnOrderHint] to determine column order
 */
class InsertStatementFiller(private val connectorRepository: ConnectorRepository, targetConnectorId: String) {
  private val closeables = ArrayList<Closeable>()
  private val indicator = connectorRepository.hint<TableCopyProgressIndicator>(targetConnectorId)

  fun fillInsertStatementFromResultSet(
    sourceTableMetaData: TableMetaData, targetTableMetaData: TableMetaData,
    targetDatabaseConfiguration: TargetDatabaseConfiguration, targetConnection: Connection,
    rs: ResultSet, insertStatement: PreparedStatement, numberOfRowsPerBatch: Int,
    useMultipleValuesClauses: Boolean
  ) {
    val sourceColumns = getSortedColumns(connectorRepository, sourceTableMetaData)
    val targetConnectorId = targetTableMetaData.connectorId
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val filter = connectorRepository.hint<TableRowDataFilter>(targetConnectorId)
    val targetDatabase = targetTableMetaData.databaseMetaData
    var targetColumnIndex = 1
    var dataItemsCount = 0

    for (currentRow in 0 until numberOfRowsPerBatch) {
      val ok = rs.next()
      val sourceValues = LinkedHashMap<ColumnMetaData, Any?>()
      val targetValues = LinkedHashMap<ColumnMetaData, Any?>()
      var insertData = true

      if (!ok) {
        throw MissingDataException(
          "No more data in row $currentRow/$numberOfRowsPerBatch in ${sourceTableMetaData.tableName}"
        )
      }

      targetDatabaseConfiguration.beforeNewRow(targetConnection, targetConnectorId, targetTableMetaData)

      for (columnIndex in 1..sourceColumns.size) {
        val sourceColumn = sourceColumns[columnIndex - 1]
        val mapping = columnMapper.map(sourceColumn, targetTableMetaData)

        if (mapping.columns.isEmpty()) {
          if (mapping.isEmptyColumnListOk) {
            indicator.warn("Dropping column $sourceColumn")
            // Unused result, but we may have to skip the next data item from an underlying stream implementation
            rs.getObject(columnIndex)

            if (!sourceColumn.isNullable) {
              indicator.warn(
                """
                $sourceColumn does not allow null values, but value is ignored for target table.
                Make sure that the column is omitted during target table creation, too, or has a default value set.
                """.trimIndent()
              )
            }
          } else {
            throw IncompatibleColumnsException(
              "Cannot map column $targetTableMetaData:$sourceColumn: Target column list empty"
            )
          }
        }

        for (targetColumn in mapping.columns) {
          val columnMapping = findMapping(sourceColumn, targetColumn)
          val columnTypeMapping = columnMapping.columnDataMapping
          val sourceValue = columnTypeMapping.sourceColumnType.getValue(rs, columnIndex)
          val targetValue = columnTypeMapping.columnDataMapper.map(columnMapping, sourceValue)
          val optionalCloseableObject = columnTypeMapping.targetColumnType.setValue(
            insertStatement, targetColumnIndex++, targetDatabase, targetColumn, targetValue
          )

          sourceValues[sourceColumn] = sourceValue
          targetValues[targetColumn] = targetValue

          if (optionalCloseableObject != null) {
            closeables.add(optionalCloseableObject)
          }

          dataItemsCount++
        }
      }

      // reset insert statement
      if (!filter.accept(sourceValues, targetValues)) {
        if (useMultipleValuesClauses) {
          throw SQLException(
            """${TableRowDataFilter::class.java.name} hint must not be used, if NumberOfRowsPerBatch hint allows multiple VALUES(...) clauses.
              Please disable it for table $targetTableMetaData""".trimIndent()
          )
        }

        insertData = false
      }

      // Add INSERTs with single VALUES clause to BATCH, see below for other case
      if (!useMultipleValuesClauses) {
        if (insertData) {
          insertStatement.addBatch()
        }

        targetColumnIndex = 1
      }

      targetDatabaseConfiguration.afterNewRow(targetConnection, targetConnectorId, targetTableMetaData)
    }

    // Add single INSERT with many VALUES clauses to BATCH
    // If it is just a single row of data to be copied, do not use batching. Some driver (MSSQL 🙄 in particular)
    // are buggy and cannot handle single row batches

    if (useMultipleValuesClauses && numberOfRowsPerBatch > 1) {
      insertStatement.addBatch()
    }

    indicator.debug("Number of data items: $dataItemsCount")
  }

  private fun findMapping(sourceColumn: ColumnMetaData, targetColumn: ColumnMetaData): ColumnMapping =
    ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(sourceColumn, targetColumn)
      ?: throw IncompatibleColumnsException(
        """|
          |Columns have incompatible types: ${sourceColumn.columnName}/${sourceColumn.columnTypeName}, ${sourceColumn.jdbcColumnType}
          |vs.                              ${targetColumn.columnName}/${targetColumn.columnTypeName}, ${targetColumn.jdbcColumnType} 
          |in table ${sourceColumn.tableMetaData.tableName}
          |Please add a mapping using ${DefaultColumnDataMapperProvider::class.java.name}
       });
      """.trimMargin()
      )

  /**
   * Clear any resources associated with this commit, open BLOBs in particular.
   */
  fun clear() {
    for (closeableObject in closeables) {
      try {
        closeableObject.close()
      } catch (e: IOException) {
        indicator.warn("While closing $closeableObject: $e")
      }
    }

    closeables.clear()
  }
}
