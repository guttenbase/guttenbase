package io.github.guttenbase.schema

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.IncompatibleTablesException
import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.schema.comparison.DuplicateIndexIssue
import io.github.guttenbase.schema.comparison.SchemaComparatorTool
import io.github.guttenbase.schema.comparison.SchemaCompatibilityIssueType
import io.github.guttenbase.tools.TableOrderTool
import java.sql.SQLException
import java.util.*
import kotlin.math.abs

/**
 * Create Custom DDL script from given database meta data.
 *
 * 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate")
class SchemaScriptCreatorTool(
  private val connectorRepository: ConnectorRepository,
  val sourceConnectorId: String,
  val targetConnectorId: String
) {
  private val databaseMetaData: DatabaseMetaData
    get() = connectorRepository.getDatabaseMetaData(sourceConnectorId)

  fun createTableStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)

    return createTableStatements(tables)
  }

  fun createTableStatements(tables: List<TableMetaData>) = tables.map { createTable(it) }

  fun createPrimaryKeyStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)

    return createPrimaryKeyStatements(tables)
  }

  fun createPrimaryKeyStatements(tables: List<TableMetaData>): List<String> = tables.map {
    it.primaryKeyColumns.map { column -> createPrimaryKeyStatement(column.tableMetaData, column.tableMetaData.primaryKeyColumns) }
  }.flatten()


  fun createIndexStatements(): List<String> {
    val tables: List<TableMetaData> = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)
    return createIndexStatements(tables)
  }

  fun createIndexStatements(tables: List<TableMetaData>): List<String> {
    val result = ArrayList<String>()

    for (tableMetaData in tables) {
      var counter = 1
      val issues = SchemaComparatorTool(connectorRepository).checkDuplicateIndexes(tableMetaData)
      val conflictedIndexes: List<IndexMetaData> = issues.compatibilityIssues
        .filter { it.compatibilityIssueType === SchemaCompatibilityIssueType.DUPLICATE_INDEX }
        .map { (it as DuplicateIndexIssue).indexMetaData }

      for (indexMetaData in tableMetaData.indexes) {
        val columns = indexMetaData.columnMetaData
        val columnsFormPrimaryKey = columns.map(ColumnMetaData::isPrimaryKey).reduce { a: Boolean, b: Boolean -> a && b }
        val conflictedIndex = conflictedIndexes.contains(indexMetaData)

        if (!columnsFormPrimaryKey && !conflictedIndex) {
          result.add(createIndex(indexMetaData, counter++))
        }
      }
    }

    return result
  }

  fun createForeignKeyStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)
    return createForeignKeyStatements(tables)
  }

  fun createForeignKeyStatements(tables: List<TableMetaData>): List<String> =
    tables.map { it.importedForeignKeys.map { foreignKey -> createForeignKey(foreignKey) } }.flatten()

  fun createTable(tableMetaData: TableMetaData): String {
    val tableName = getTableName(tableMetaData)
    return ("CREATE TABLE $tableName\n(\n" + tableMetaData.columnMetaData.joinToString(separator = ",\n",
      transform = { "  " + createColumn(it) })) + "\n);"
  }

  private fun getTableName(tableMetaData: TableMetaData): String {
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val rawTableName: String = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val tableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
    val maxNameLength = targetMaxNameLength

    if (rawTableName.length > maxNameLength) {
      throw IncompatibleTablesException(
        "Table name " + rawTableName + " is too long for the targeted data base (Max. "
            + maxNameLength + "). You will have to provide an appropriate " + TableMapper::class.java.name + " hint"
      )
    }

    return tableName
  }

  private fun createPrimaryKeyStatement(tableMetaData: TableMetaData, primaryKeyColumns: List<ColumnMetaData>): String {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val columnMapper: ColumnMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java)
      .value
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val qualifiedTableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
    val tableName: String = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val pkName = createConstraintName("PK_", tableName, "")

    return "ALTER TABLE $qualifiedTableName ADD CONSTRAINT $pkName PRIMARY KEY (" + primaryKeyColumns.joinToString(
      separator = ", ", transform = { columnMapper.mapColumnName(it, tableMetaData) }) + ");"
  }

  private fun createIndex(indexMetaData: IndexMetaData, counter: Int): String {
    val tableMetaData: TableMetaData = indexMetaData.tableMetaData
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableName: String = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val indexName = createConstraintName(
      "IDX_", CaseConversionMode.UPPER.convert(indexMetaData.indexName)
          + "_" + tableName + "_", counter
    )
    return createIndex(indexMetaData, indexName)
  }

  fun createIndex(indexMetaData: IndexMetaData) = createIndex(indexMetaData, indexMetaData.indexName)

  private fun createIndex(indexMetaData: IndexMetaData, indexName: String): String {
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val columnMapper: ColumnMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value
    val tableMetaData: TableMetaData = indexMetaData.tableMetaData
    val unique = if (indexMetaData.isUnique) " UNIQUE " else " "

    return ("CREATE" + unique + "INDEX " + indexName + " ON " + tableMapper.fullyQualifiedTableName(
      tableMetaData, targetDatabaseMetaData
    )) +
        " (" + indexMetaData.columnMetaData.joinToString(
      separator = ", ",
      transform = { columnMapper.mapColumnName(it, tableMetaData) }) + ");"
  }

  fun createForeignKey(foreignKeyMetaData: ForeignKeyMetaData): String {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMetaData: TableMetaData = foreignKeyMetaData.referencingTableMetaData
    val qualifiedTableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
    return (("ALTER TABLE " + qualifiedTableName + " ADD CONSTRAINT " + foreignKeyMetaData.foreignKeyName + " FOREIGN KEY "
        + foreignKeyMetaData.referencingColumns.joinToString(separator = ", ", prefix = "(", postfix = ")",
      transform = { getColumnName(it) })
        ) + " REFERENCES "
        + tableMapper.fullyQualifiedTableName(foreignKeyMetaData.referencedTableMetaData, targetDatabaseMetaData)
        + foreignKeyMetaData.referencedColumns.joinToString(separator = ", ", prefix = "(", postfix = ")",
      transform = { getColumnName(it) })) + ";"
  }

  private fun getColumnName(referencingColumn: ColumnMetaData): String {
    val columnMapper: ColumnMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value
    return columnMapper.mapColumnName(referencingColumn, referencingColumn.tableMetaData)
  }

  fun createColumn(columnMetaData: ColumnMetaData): String {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val columnMapper: ColumnMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value
    val columnTypeMapper: ColumnTypeMapper =
      connectorRepository.getConnectorHint(targetConnectorId, ColumnTypeMapper::class.java).value
    val builder = StringBuilder()
    val sourceType: DatabaseType = connectorRepository.getDatabaseMetaData(sourceConnectorId).databaseType
    val targetType: DatabaseType = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseType
    val columnName: String = columnMapper.mapColumnName(columnMetaData, columnMetaData.tableMetaData)
    val columnType: String = columnTypeMapper.mapColumnType(columnMetaData, sourceType, targetType)
    val maxNameLength = targetMaxNameLength
    val rawTableName: String = tableMapper.mapTableName(columnMetaData.tableMetaData, targetDatabaseMetaData)

    if (columnName.length > maxNameLength) {
      throw IncompatibleColumnsException(
        "Table " + rawTableName + ": Column name " + columnName + " is too long for " +
            "the targeted data base (Max. " + maxNameLength + ")." +
            " You will have to provide an appropriate " + ColumnMapper::class.java.name + " hint"
      )
    }

    builder.append("$columnName $columnType")

    return builder.toString()
  }

  fun createConstraintName(prefix: String, preferredName: String?, uniqueId: Any): String {
    val name = StringBuilder(preferredName)
    val maxLength = targetMaxNameLength - prefix.length - uniqueId.toString().length

    while (name.length > maxLength) {
      val index = abs(RANDOM.nextInt() % name.length)
      name.deleteCharAt(index)
    }

    return prefix + name + uniqueId
  }

  val targetMaxNameLength: Int
    get() {
      val metaData = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseMetaData

      // Since there is no getMaxConstraintNameLength() ...
      val nameLength = getMaxColumnNameLength(metaData)

      // Return reasonable default if value is not known or unlimited
      return if (nameLength <= 0) 64 else nameLength
    }

  private fun getMaxColumnNameLength(metaData: java.sql.DatabaseMetaData): Int {
    return try {
      metaData.maxColumnNameLength
    } catch (e: SQLException) {
      throw GuttenBaseException("getMaxColumnNameLength", e)
    }
  }

  fun createTableColumn(columnMetaData: ColumnMetaData) =
    "ALTER TABLE " + getTableName(columnMetaData.tableMetaData) + " ADD " + createColumn(columnMetaData) + ";"

  companion object {
    private val RANDOM = Random()
  }
}
