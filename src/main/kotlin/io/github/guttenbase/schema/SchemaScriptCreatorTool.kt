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
import org.slf4j.LoggerFactory
import java.sql.SQLException
import java.util.*
import kotlin.math.abs

/**
 * Create Custom DDL script from given database meta data.
 *
 * 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("MemberVisibilityCanBePrivate")
class SchemaScriptCreatorTool(
  private val connectorRepository: ConnectorRepository,
  private val sourceConnectorId: String, private val targetConnectorId: String
) {
  private val sourcedatabaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(sourceConnectorId) }
  private val targetDatabaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(targetConnectorId) }
  private val tables get() = TableOrderTool(sourcedatabaseMetaData).orderTables()

  fun createTableStatements() = createTableStatements(tables)

  fun createTableStatements(tables: List<TableMetaData>) = tables.map { createTable(it) }

  @Suppress("unused")
  fun createMultiColumnPrimaryKeyStatements() = createMultiColumnPrimaryKeyStatements(tables)

  fun createMultiColumnPrimaryKeyStatements(tables: List<TableMetaData>) = tables
    .filter { it.primaryKeyColumns.size > 1 }
    .map { createPrimaryKeyStatement(it, it.primaryKeyColumns) }

  fun createIndexStatements() = createIndexStatements(tables)

  fun createIndexStatements(tables: List<TableMetaData>): List<String> {
    val result = ArrayList<String>()

    for (tableMetaData in tables) {
      var counter = 1
      val issues = SchemaComparatorTool(connectorRepository, sourceConnectorId, targetConnectorId)
        .checkDuplicateIndexes(tableMetaData)
      val conflictedIndexes = issues.compatibilityIssues
        .filter { it.compatibilityIssueType === SchemaCompatibilityIssueType.DUPLICATE_INDEX }
        .map { (it as DuplicateIndexIssue).indexMetaData }

      for (indexMetaData in tableMetaData.indexes) {
        val columns = indexMetaData.columnMetaData
        val columnsFormPrimaryKey =
          columns.map(ColumnMetaData::isPrimaryKey).reduce { a: Boolean, b: Boolean -> a && b }
        val conflictedIndex = conflictedIndexes.contains(indexMetaData)
        // Most DBs do not support indexes on binary columns
        val containsBinaryType = columns
          .any { it.jdbcColumnType.isBinaryType() || it.jdbcColumnType.isBlobType() || it.jdbcColumnType.isClobType() }

        if (!columnsFormPrimaryKey && !conflictedIndex) {
          if (containsBinaryType && targetDatabaseMetaData.databaseType != DatabaseType.MYSQL) {
            LOG.warn("Skipping index ${indexMetaData.indexName} on table ${tableMetaData.tableName} because it contains binary columns")
          } else {
            result.add(createIndex(indexMetaData, counter++))
          }
        }
      }
    }

    return result
  }

  fun createForeignKeyStatements() = createForeignKeyStatements(tables)

  fun createForeignKeyStatements(tables: List<TableMetaData>) = tables
    .map { it.importedForeignKeys.map { foreignKey -> createForeignKey(foreignKey) } }.flatten()

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
        primaryKeyColumns.joinToString {
          val rawColumnName = columnMapper.mapColumnName(it, tableMetaData)
          targetDatabaseMetaData.databaseType.escapeDatabaseEntity(rawColumnName)
        } + ";"
  }

  private fun createIndex(indexMetaData: IndexMetaData, counter: Int): String {
    val tableMetaData = indexMetaData.tableMetaData
    val indexMapper = connectorRepository.hint<IndexMapper>(targetConnectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableName = tableMapper.mapTableName(tableMetaData, targetDatabaseMetaData)
    val indexName = createConstraintName(
      SYNTHETIC_INDEX_PREFIX, indexMapper.mapIndexName(indexMetaData) + "_" + tableName + "_", counter
    )

    return createIndex(indexMetaData, indexName)
  }

  fun createIndex(indexMetaData: IndexMetaData): String {
    val indexMapper = connectorRepository.hint<IndexMapper>(targetConnectorId)

    return createIndex(indexMetaData, indexMapper.mapIndexName(indexMetaData))
  }

  private fun createIndex(index: IndexMetaData, indexName: String): String {
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val databaseType = targetDatabaseMetaData.databaseType
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    val tableMetaData = index.tableMetaData
    val unique = if (index.isUnique) " UNIQUE " else " "
    val containsClob = index.columnMetaData.any { it.jdbcColumnType.isClobType() }
    val fulltext = if (databaseType == DatabaseType.MYSQL && containsClob) " FULLTEXT " else ""

    return ("CREATE" + unique + fulltext + "INDEX " + databaseType.escapeDatabaseEntity(indexName) + " ON "
        + tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)) +
        index.columnMetaData.joinToString {
          val rawColumnName = columnMapper.mapColumnName(it, tableMetaData)
          targetDatabaseMetaData.databaseType.escapeDatabaseEntity(rawColumnName)
        } + ";"
  }

  fun createForeignKey(foreignKeyMetaData: ForeignKeyMetaData): String {
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val fkMapper = connectorRepository.hint<ForeignKeyMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val tableMetaData = foreignKeyMetaData.referencingTableMetaData
    val qualifiedTableName = tableMapper.fullyQualifiedTableName(tableMetaData, targetDatabaseMetaData)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)

    return (("ALTER TABLE " + qualifiedTableName + " ADD CONSTRAINT " + fkMapper.mapForeignKeyName(foreignKeyMetaData) + " FOREIGN KEY "
        + foreignKeyMetaData.referencingColumns.joinToString {
      val rawColumnName = columnMapper.mapColumnName(it, it.tableMetaData)
      targetDatabaseMetaData.databaseType.escapeDatabaseEntity(rawColumnName)
    }
        ) + " REFERENCES "
        + tableMapper.fullyQualifiedTableName(foreignKeyMetaData.referencedTableMetaData, targetDatabaseMetaData)
        + foreignKeyMetaData.referencedColumns.joinToString {
      val rawColumnName = columnMapper.mapColumnName(it, it.tableMetaData)
      targetDatabaseMetaData.databaseType.escapeDatabaseEntity(rawColumnName)
    }) + ";"
  }

  fun createAutoincrementUpdateStatements() = createAutoincrementUpdateStatements(tables)

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
    val rawColumnName = columnMapper.mapColumnName(columnMetaData, columnMetaData.tableMetaData)
    val columnName = targetDatabase.databaseType.escapeDatabaseEntity(rawColumnName)
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

  fun createConstraintName(prefix: String, preferredName: String, uniqueId: Any): String {
    val name = StringBuilder(preferredName)
    val maxLength = targetMaxNameLength - prefix.length - uniqueId.toString().length

    while (name.length > maxLength) {
      val index = abs(RANDOM.nextInt() % name.length)
      name.deleteCharAt(index)
    }

    val finalName = name.toString()

    return if (finalName.startsWith(prefix, true)) {
      finalName + uniqueId
    } else {
      prefix + finalName + uniqueId
    }
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
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(SchemaScriptCreatorTool::class.java)

    private val RANDOM = Random()
  }
}
