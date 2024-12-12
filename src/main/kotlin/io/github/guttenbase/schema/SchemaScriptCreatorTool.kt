package io.github.guttenbase.schema

import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.IncompatibleTablesException
import io.github.guttenbase.mapping.*
import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import io.github.guttenbase.repository.hint
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
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate")
class SchemaScriptCreatorTool(
  private val connectorRepository: ConnectorRepository,
  private val sourceConnectorId: String, private val targetConnectorId: String
) {
  private val databaseMetaData: DatabaseMetaData
    get() = connectorRepository.getDatabaseMetaData(sourceConnectorId)

  fun createTableStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)

    return createTableStatements(tables)
  }

  fun createTableStatements(tables: List<TableMetaData>) = tables.map { createTable(it) }

  @Suppress("unused")
  fun createMultiColumnPrimaryKeyStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)

    return createMultiColumnPrimaryKeyStatements(tables)
  }

  fun createMultiColumnPrimaryKeyStatements(tables: List<TableMetaData>): List<String> =
    tables.filter { it.primaryKeyColumns.size > 1 }.map { createPrimaryKeyStatement(it, it.primaryKeyColumns) }

  fun createIndexStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)
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
        val columnsFormPrimaryKey =
          columns.map(ColumnMetaData::isPrimaryKey).reduce { a: Boolean, b: Boolean -> a && b }
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
    val databaseType = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseType
    val notExistsClause = (" " + databaseType.tableNotExistsClause).trimEnd()

    return ("CREATE TABLE$notExistsClause $tableName\n" +
        tableMetaData.columnMetaData.joinToString(newline = true) { "  " + createColumn(it) }
        ) + ";"
  }

  private fun getTableName(tableMetaData: TableMetaData): String {
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val rawTableName = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
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
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val qualifiedTableName = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
    val tableName = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val pkName = createConstraintName("PK_", tableName, "")

    return "ALTER TABLE $qualifiedTableName ADD CONSTRAINT $pkName PRIMARY KEY " +
        primaryKeyColumns.joinToString { columnMapper.mapColumnName(it, tableMetaData) } + ";"
  }

  private fun createIndex(indexMetaData: IndexMetaData, counter: Int): String {
    val tableMetaData = indexMetaData.tableMetaData
    val indexMapper = connectorRepository.hint<IndexMapper>(targetConnectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableName = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val indexName = createConstraintName(
      "IDX_", indexMapper.mapIndexName(indexMetaData) + "_" + tableName + "_", counter
    )

    return createIndex(indexMetaData, indexName)
  }

  fun createIndex(indexMetaData: IndexMetaData): String {
    val indexMapper = connectorRepository.hint<IndexMapper>(targetConnectorId)

    return createIndex(indexMetaData, indexMapper.mapIndexName(indexMetaData))
  }

  private fun createIndex(indexMetaData: IndexMetaData, indexName: String): String {
    val targetDatabaseMetaData: DatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val tableMetaData = indexMetaData.tableMetaData
    val unique = if (indexMetaData.isUnique) " UNIQUE " else " "

    return ("CREATE" + unique + "INDEX " + indexName + " ON " + tableMapper.fullyQualifiedTableName(
      tableMetaData, targetDatabaseMetaData
    )) + indexMetaData.columnMetaData.joinToString { columnMapper.mapColumnName(it, tableMetaData) } + ";"
  }

  fun createForeignKey(foreignKeyMetaData: ForeignKeyMetaData): String {
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val fkMapper = connectorRepository.hint<ForeignKeyMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMetaData = foreignKeyMetaData.referencingTableMetaData
    val qualifiedTableName = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)

    return (("ALTER TABLE " + qualifiedTableName + " ADD CONSTRAINT " + fkMapper.mapForeignKeyName(foreignKeyMetaData) + " FOREIGN KEY "
        + foreignKeyMetaData.referencingColumns.joinToString { getColumnName(it) }
        ) + " REFERENCES "
        + tableMapper.fullyQualifiedTableName(foreignKeyMetaData.referencedTableMetaData, targetDatabaseMetaData)
        + foreignKeyMetaData.referencedColumns.joinToString { getColumnName(it) }) + ";"
  }

  private fun getColumnName(referencingColumn: ColumnMetaData): String {
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)

    return columnMapper.mapColumnName(referencingColumn, referencingColumn.tableMetaData)
  }

  fun createAutoincrementUpdateStatements(): List<String> {
    val tables = TableOrderTool().getOrderedTables(databaseMetaData.tableMetaData, true)

    return createAutoincrementUpdateStatements(tables)
  }

  fun createAutoincrementUpdateStatements(tables: List<TableMetaData>): List<String> {
    val result = ArrayList<String>()
    val databaseType = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseType

    for (table in tables) {
      val column = table.getNumericPrimaryKeyColumn()

      if (column != null && column.isAutoIncrement) {
        val statement = databaseType.createColumnAutoincrementStatement(column)

        if (statement != null) {
          result.add(statement)
        }
      }
    }

    return result
  }

  fun createColumn(columnMetaData: ColumnMetaData): String {
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val columnTypeMapper = connectorRepository.hint<ColumnTypeMapper>(targetConnectorId)
    val sourceDatabase = connectorRepository.getDatabaseMetaData(sourceConnectorId)
    val targetDatabase = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val columnName = columnMapper.mapColumnName(columnMetaData, columnMetaData.tableMetaData)
    val columnType = columnTypeMapper.mapColumnType(columnMetaData, sourceDatabase, targetDatabase)
    val maxNameLength = targetMaxNameLength
    val rawTableName = tableMapper.mapTableName(columnMetaData.tableMetaData, targetDatabase)

    if (columnName.length > maxNameLength) {
      throw IncompatibleColumnsException(
        "Table " + rawTableName + ": Column name " + columnName + " is too long for " +
            "the targeted data base (Max. " + maxNameLength + ")." +
            " You will have to provide an appropriate " + ColumnMapper::class.java.name + " hint"
      )
    }

    return "$columnName $columnType"
  }

  fun addTableColumn(columnMetaData: ColumnMetaData) =
    "ALTER TABLE " + getTableName(columnMetaData.tableMetaData) + " ADD " + createColumn(columnMetaData) + ";"

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

  private fun getMaxColumnNameLength(metaData: JdbcDatabaseMetaData): Int {
    return try {
      metaData.maxColumnNameLength
    } catch (e: SQLException) {
      throw GuttenBaseException("getMaxColumnNameLength", e)
    }
  }

  private fun List<ColumnMetaData>.joinToString(newline: Boolean = false, mapper: (ColumnMetaData) -> String): String {
    val nl = if (newline) "\n" else ""

    return joinToString(
      separator = ", $nl", prefix = "($nl", postfix = "$nl)",
      transform = mapper
    )
  }

  companion object {
    private val RANDOM = Random()
  }
}
