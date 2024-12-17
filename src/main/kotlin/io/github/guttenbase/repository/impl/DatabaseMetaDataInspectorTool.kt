package io.github.guttenbase.repository.impl

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.meta.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.impl.*
import io.github.guttenbase.repository.*
import io.github.guttenbase.tools.SelectWhereClause
import io.github.guttenbase.utils.Util
import org.slf4j.LoggerFactory
import java.lang.reflect.Method
import java.sql.*

/**
 * Get table meta data from connection.
 *
 * (C) 2012-2045 by akquinet tech@spree
 *
 * @author M. Dahm
 */
internal class DatabaseMetaDataInspectorTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String
) {
  internal fun getDatabaseMetaData(connection: Connection): DatabaseMetaData {
    val connectionInfo: ConnectorInfo = connectorRepository.getConnectionInfo(connectorId)

    LOG.info("Retrieving meta data for $connectorId:$connectionInfo")

    val schema = connectionInfo.schema
    val schemaPrefix = if ("" == Util.trim(schema)) "" else "$schema."
    val metaData = connection.metaData
    val properties = JdbcDatabaseMetaData::class.java.declaredMethods
      .filter { method -> method.parameterCount == 0 && isPrimitive(method.returnType) }
      .mapNotNull { method -> getValue(method, metaData) }.toMap()
    val result = DatabaseMetaDataImpl(connectorRepository, connectorId, schema, properties, connectionInfo.databaseType)

    loadSupportedTypes(result, metaData)
    loadTables(result, metaData)
    updateTableMetaData(connection, metaData, result, schemaPrefix)

    LOG.info("Retrieving meta data for $connectorId DONE")

    return result
  }

  private fun updateTableMetaData(
    connection: Connection, metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData, schemaPrefix: String
  ) {
    connection.createStatement().use { statement ->
      for (table in databaseMetaData.tableMetaData) {
        val tableMetaData = table as InternalTableMetaData

        updateTableMetaDataWithColumnInformation(statement, tableMetaData, schemaPrefix)
      }
    }

    try {
      for (table in databaseMetaData.tableMetaData) {
        val tableMetaData = table as InternalTableMetaData

        updateColumnsWithPrimaryKeyInformation(metaData, databaseMetaData, tableMetaData)
        updateColumnsWithForeignKeyInformation(metaData, databaseMetaData, tableMetaData)
        updateTableWithIndexInformation(metaData, databaseMetaData, tableMetaData)
      }
    } catch (e: Exception) {
      // Some drivers such as JdbcOdbcBridge do not support this
      LOG.warn("Could not update additional schema information", e)
    }

    connection.createStatement().use { statement ->
      for (table in databaseMetaData.tableMetaData) {
        val tableMetaData = table as InternalTableMetaData

        enrichTableMetaData(statement, tableMetaData, schemaPrefix)
      }
    }
  }

  private fun updateColumnsWithForeignKeyInformation(
    metaData: JdbcDatabaseMetaData,
    databaseMetaData: DatabaseMetaData,
    table: TableMetaData
  ) {
    LOG.debug("Retrieving foreign key information for " + table.tableName)

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val fkFilter = connectorRepository.hint<DatabaseForeignKeyFilter>(connectorId)
    val resultSet = metaData.getExportedKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchemaPattern(databaseMetaData),
      table.tableName
    )

    resultSet.use {
      while (resultSet.next()) {
        val pkTableName = resultSet.getStringNotNull("PKTABLE_NAME")
        val pkColumnName = resultSet.getStringNotNull("PKCOLUMN_NAME")
        val fkTableName = resultSet.getStringNotNull("FKTABLE_NAME")
        val fkColumnName = resultSet.getStringNotNull("FKCOLUMN_NAME")
        val fkName = resultSet.getString("FK_NAME") ?: "${SYNTHETIC_CONSTRAINT_PREFIX}UNKNOWN_$fkColumnName"
        val pkTableMetaData = databaseMetaData.getTableMetaData(pkTableName) as InternalTableMetaData?
        val fkTableMetaData = databaseMetaData.getTableMetaData(fkTableName) as InternalTableMetaData?

        if (fkTableMetaData == null || pkTableMetaData == null) {
          // this table might have been excluded from the list of tables handled by this batch
          LOG.warn("Unable to retrieve metadata information for table $fkTableName referenced by $pkTableName")
        } else {
          val pkColumn = pkTableMetaData.getColumnMetaData(pkColumnName)!!
          val fkColumn = fkTableMetaData.getColumnMetaData(fkColumnName)!!
          var exportedForeignKey = pkTableMetaData.getExportedForeignKey(fkName) as InternalForeignKeyMetaData?
          var importedForeignKey = fkTableMetaData.getImportedForeignKey(fkName) as InternalForeignKeyMetaData?

          if (exportedForeignKey == null) {
            exportedForeignKey = ForeignKeyMetaDataImpl(pkTableMetaData, fkName, fkColumn, pkColumn)

            if (fkFilter.accept(exportedForeignKey)) {
              pkTableMetaData.addExportedForeignKey(exportedForeignKey)
            }
          } else {
            exportedForeignKey.addColumnTuple(fkColumn, pkColumn)
          }

          if (importedForeignKey == null) {
            importedForeignKey = ForeignKeyMetaDataImpl(fkTableMetaData, fkName, fkColumn, pkColumn)

            if (fkFilter.accept(importedForeignKey)) {
              fkTableMetaData.addImportedForeignKey(importedForeignKey)
            }
          } else {
            importedForeignKey.addColumnTuple(fkColumn, pkColumn)
          }
        }
      }
    }
  }

  private fun updateTableWithIndexInformation(
    metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData,
    table: InternalTableMetaData
  ) {
    LOG.debug("Retrieving index information for " + table.tableName)

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val indexFilter = connectorRepository.hint<DatabaseIndexFilter>(connectorId)
    val resultSet = metaData.getIndexInfo(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), table.tableName, false,
      true
    )

    resultSet.use {
      while (resultSet.next()) {
        val nonUnique = resultSet.getBoolean("NON_UNIQUE")
        val columnName = resultSet.getString("COLUMN_NAME")
        val indexName = resultSet.getString("INDEX_NAME") ?: "${SYNTHETIC_INDEX_PREFIX}UNKNOWN_$columnName"
        val ascOrDesc = resultSet.getString("ASC_OR_DESC")

        if (columnName != null) {
          val column = table.getColumnMetaData(columnName)

          if (column != null && !table.hasForeignKey(indexName)) {
            var indexMetaData = table.getIndexMetaData(indexName) as InternalIndexMetaData?

            if (indexMetaData == null) {
              val ascending = ascOrDesc == null || "A" == ascOrDesc
              val unique = !nonUnique

              indexMetaData = IndexMetaDataImpl(table, indexName, ascending, unique, column.isPrimaryKey)
              indexMetaData.addColumn(column)

              if (indexFilter.accept(indexMetaData)) {
                table.addIndex(indexMetaData)
              }
            } else {
              indexMetaData.addColumn(column)
            }
          }
        }
      }
    }
  }

  private fun TableMetaData.hasForeignKey(name: String) =
    exportedForeignKeys.map { it.foreignKeyName.uppercase() }.contains(name.uppercase())
        || importedForeignKeys.map { it.foreignKeyName.uppercase() }.contains(name.uppercase())

  @Throws(SQLException::class)
  private fun updateColumnsWithPrimaryKeyInformation(
    metaData: JdbcDatabaseMetaData,
    databaseMetaData: DatabaseMetaData,
    table: TableMetaData
  ) {
    LOG.debug("Retrieving primary key information for " + table.tableName)

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val resultSet = metaData.getPrimaryKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), table.tableName
    )

    resultSet.use {
      while (resultSet.next()) {
        val pkName = resultSet.getString("PK_NAME")
        val columnName =
          resultSet.getString("COLUMN_NAME") ?: throw GuttenBaseException("COLUMN_NAME must not be null")

        if (pkName != null) {
          val columnMetaData = table.getColumnMetaData(columnName) as InternalColumnMetaData?
            ?: throw IllegalStateException("No column meta data for $columnName")
          columnMetaData.isPrimaryKey = true
        }
      }
    }
  }

  private fun updateTableMetaDataWithColumnInformation(
    statement: Statement, tableMetaData: InternalTableMetaData, schemaPrefix: String
  ) {
    val databaseType = tableMetaData.databaseMetaData.databaseType
    val tableName = databaseType.escapeDatabaseEntity(tableMetaData.tableName, schemaPrefix)
    val columnFilter = connectorRepository.hint<DatabaseColumnFilter>(connectorId)
    val selectSQL = SELECT_NOTHING_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)

    LOG.debug("Retrieving column information for $tableName")

    val resultSet = statement.executeQuery(selectSQL)

    resultSet.use {
      val meta = resultSet.metaData
      val columnCount = meta.columnCount

      for (i in 1..columnCount) {
        val columnTypeName = meta.getColumnTypeName(i)
        val columnType = meta.getColumnType(i)
        val columnName = meta.getColumnName(i)
        val columnClassName = meta.getColumnClassName(i)
        val isNullable = meta.isNullable(i) != ResultSetMetaData.columnNoNulls
        val isAutoIncrement = meta.isAutoIncrement(i)
        val precision = meta.getPrecision(i)
        val scale = meta.getScale(i)
        val column = ColumnMetaDataImpl(
          tableMetaData,
          columnType,
          columnName,
          columnTypeName,
          columnClassName,
          isNullable,
          isAutoIncrement,
          precision,
          scale
        )

        if (columnFilter.accept(column)) {
          tableMetaData.addColumn(column)
        }
      }
    }
  }

  private fun createWhereClause(tableMetaData: TableMetaData) =
    connectorRepository.hint<SelectWhereClause>(connectorId).getWhereClause(tableMetaData)

  private fun enrichTableMetaData(
    statement: Statement,
    tableMetaData: InternalTableMetaData,
    schemaPrefix: String
  ) {
    val filter = connectorRepository.hint<TableRowCountFilter>(connectorId)

    if (filter.accept(tableMetaData)) {
      val databaseType = tableMetaData.databaseMetaData.databaseType
      val tableName = databaseType.escapeDatabaseEntity(tableMetaData.tableName, schemaPrefix)
      LOG.debug("Retrieving row count for $tableName")

      computeRowCount(tableName, tableMetaData, statement)

      val primaryKeyColumn = tableMetaData.getNumericPrimaryKeyColumn()

      if (primaryKeyColumn != null) {
        val maxIdStatement = SELECT_MIN_MAX_ID_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)
          .replace(COLUMN_PLACEHOLDER, databaseType.escapeDatabaseEntity(primaryKeyColumn.columnName))

        statement.executeQuery(maxIdStatement).use {
          it.next()
          tableMetaData.minId = it.getLong(1)
          tableMetaData.maxId = it.getLong(2)
        }
      }
    } else {
      tableMetaData.totalRowCount = filter.defaultRowCount(tableMetaData)
      tableMetaData.filteredRowCount = filter.defaultRowCount(tableMetaData)
      tableMetaData.minId = filter.defaultMinId(tableMetaData)
      tableMetaData.maxId = filter.defaultMaxId(tableMetaData)
    }
  }

  private fun computeRowCount(
    tableName: String,
    tableMetaData: InternalTableMetaData,
    statement: Statement
  ) {
    val countAllSQL = SELECT_COUNT_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)
    val filterClause = createWhereClause(tableMetaData).trim { it <= ' ' }
    val countFilteredSQL = SELECT_COUNT_STATEMENT.replace(TABLE_PLACEHOLDER, tableName) + " " + filterClause
    val totalCount = getCount(statement, countAllSQL)
    val filteredCount = if ("" == filterClause) totalCount else getCount(statement, countFilteredSQL)

    tableMetaData.totalRowCount = totalCount
    tableMetaData.filteredRowCount = filteredCount
  }

  private fun getCount(statement: Statement, countAllSQL: String): Int {
    return statement.executeQuery(countAllSQL).use {
      it.next()
      it.getInt(1)
    }
  }

  private fun loadSupportedTypes(databaseMetaData: InternalDatabaseMetaData, metaData: JdbcDatabaseMetaData) {
    LOG.debug("Look up supported types of " + databaseMetaData.databaseType)
    val resultSet = metaData.typeInfo

    resultSet.use {
      while (resultSet.next()) {
        /** @see [java.sql.DatabaseMetaData.typeInfo]
         */
        val typeName = resultSet.getString("TYPE_NAME")
        val sqlType = resultSet.getInt("DATA_TYPE")

        // Handle DB-specific types such as Oracle's VECTOR(-105)
        val jdbcType = try {
          JDBCType.valueOf(sqlType)
        } catch (_: IllegalArgumentException) {
          JDBCType.OTHER
        }
        val precision = resultSet.getInt("PRECISION")
        val nullable = resultSet.getBoolean("NULLABLE")

        databaseMetaData.addSupportedType(typeName, jdbcType, precision, nullable)
      }
    }
  }

  private fun loadTables(databaseMetaData: InternalDatabaseMetaData, metaData: JdbcDatabaseMetaData) {
    LOG.debug("Searching tables in schema " + databaseMetaData.schema)

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)

    val resultSet = metaData.getTables(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchemaPattern(databaseMetaData),
      tableFilter.getTableNamePattern(databaseMetaData),
      tableFilter.getTableTypes(databaseMetaData)
    )

    resultSet.use {
      while (resultSet.next()) {
        /** @see [java.sql.DatabaseMetaData.getTables]
         */
        val tableCatalog = resultSet.getString("TABLE_CAT")
        val tableSchema = resultSet.getString("TABLE_SCHEM")
        val tableName = resultSet.getStringNotNull("TABLE_NAME")
        val tableType = resultSet.getStringNotNull("TABLE_TYPE")

        LOG.debug("Found: $tableCatalog/$tableSchema/$tableName/$tableType")

        val tableMetaData = TableMetaDataImpl(databaseMetaData, tableName, tableType, tableCatalog, tableSchema)

        if (tableFilter.accept(tableMetaData)) {
          databaseMetaData.addTable(tableMetaData)
        }
      }

      LOG.info("${databaseMetaData.tableMetaData.size} accepted tables: ${databaseMetaData.tableMetaData}")
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(DatabaseMetaDataInspectorTool::class.java)

    private const val TABLE_PLACEHOLDER = "<table>"
    private const val COLUMN_PLACEHOLDER = "<column>"
    private const val SELECT_COUNT_STATEMENT = "SELECT COUNT(*) FROM $TABLE_PLACEHOLDER"
    private const val SELECT_MIN_MAX_ID_STATEMENT =
      "SELECT MIN($COLUMN_PLACEHOLDER), MAX($COLUMN_PLACEHOLDER) FROM $TABLE_PLACEHOLDER"
    private const val SELECT_NOTHING_STATEMENT = "SELECT * FROM $TABLE_PLACEHOLDER WHERE 1 > 2"

    private fun getValue(method: Method, data: JdbcDatabaseMetaData): Pair<String, Any>? {
      val key = method.name

      try {
        val value = method.invoke(data)

        if (value != null) {
          return key to value
        }
      } catch (e: Exception) {
        LOG.warn("Could not get meta data property:" + key + "->" + e.message)
      }

      return null
    }

    private fun isPrimitive(clazz: Class<*>) =
      clazz != Void::class.java && (clazz.isPrimitive || clazz == String::class.java)
  }
}

private fun ResultSet.getStringNotNull(name: String): String = getString(name)
  ?: throw GuttenBaseException("Column $name must not be NULL")
