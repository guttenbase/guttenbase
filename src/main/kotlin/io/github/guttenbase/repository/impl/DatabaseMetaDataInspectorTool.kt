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
import java.util.*

/**
 * Get table meta data from connection.
 *
 *
 * (C) 2012-2045 by akquinet tech@spree
 *
 * @author M. Dahm
 */
class DatabaseMetaDataInspectorTool(private val connectorRepository: ConnectorRepository, private val connectorId: String) {
  @Throws(SQLException::class)
  fun getDatabaseMetaData(connection: Connection): DatabaseMetaData {
    val connectionInfo: ConnectorInfo = connectorRepository.getConnectionInfo(connectorId)
    LOG.info("Retrieving meta data for $connectorId:$connectionInfo")

    val schema: String = connectionInfo.schema
    val schemaPrefix = if ("" == Util.trim(schema)) "" else "$schema."
    val metaData = connection.metaData
    val properties = JdbcDatabaseMetaData::class.java.declaredMethods
      .filter { method: Method -> method.parameterCount == 0 && isPrimitive(method.returnType) }
      .mapNotNull { method: Method -> getValue(method, metaData) }.toMap()
    val result = DatabaseMetaDataImpl(schema, properties, connectionInfo.databaseType)

    loadTables(result, metaData)
    updateTableMetaData(connection, metaData, result, schemaPrefix)
    LOG.info("Retrieving meta data for $connectorId DONE")

    return result
  }

  @Throws(SQLException::class)
  private fun updateTableMetaData(
    connection: Connection, metaData: JdbcDatabaseMetaData,
    databaseMetaData: DatabaseMetaData, schemaPrefix: String
  ) {
    connection.createStatement().use { statement ->
      for (table in databaseMetaData.tableMetaData) {
        val tableMetaData = table as InternalTableMetaData

        updateTableWithRowCount(statement, tableMetaData, schemaPrefix)
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
  }

  @Throws(SQLException::class)
  private fun updateColumnsWithForeignKeyInformation(
    metaData: JdbcDatabaseMetaData,
    databaseMetaData: DatabaseMetaData,
    table: TableMetaData
  ) {
    LOG.debug("Retrieving foreign key information for " + table.tableName)
    val tableFilter: DatabaseTableFilter = connectorRepository.getConnectorHint(connectorId, DatabaseTableFilter::class.java)
      .value
    val resultSet = metaData.getExportedKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchemaPattern(databaseMetaData),
      table.tableName
    )

    resultSet.use {
      while (resultSet.next()) {
        val pkTableName = resultSet.getString("PKTABLE_NAME") ?: throw GuttenBaseException("PKTABLE_NAME must not be null")
        val pkColumnName = resultSet.getString("PKCOLUMN_NAME") ?: throw GuttenBaseException("PKCOLUMN_NAME must not be null")
        val fkTableName = resultSet.getString("FKTABLE_NAME") ?: throw GuttenBaseException("FKTABLE_NAME must not be null")
        val fkColumnName = resultSet.getString("FKCOLUMN_NAME") ?: throw GuttenBaseException("FKCOLUMN_NAME must not be null")
        val fkName: String = resultSet.getString("FK_NAME") ?: "FK_UNKNOWN_$fkColumnName"
        val pkTableMetaData: InternalTableMetaData? = databaseMetaData.getTableMetaData(pkTableName) as InternalTableMetaData?
        val fkTableMetaData: InternalTableMetaData? = databaseMetaData.getTableMetaData(fkTableName) as InternalTableMetaData?

        if (fkTableMetaData == null || pkTableMetaData == null) {
          // this table might have been excluded from the list of tables handled by this batch
          LOG.warn("Unable to retrieve metadata information for table $fkTableName referenced by $pkTableName")
        } else {
          val pkColumn: ColumnMetaData = pkTableMetaData.getColumnMetaData(pkColumnName)!!
          val fkColumn: ColumnMetaData = fkTableMetaData.getColumnMetaData(fkColumnName)!!
          val exportedForeignKey: InternalForeignKeyMetaData? = pkTableMetaData
            .getExportedForeignKey(fkName) as InternalForeignKeyMetaData?
          val importedForeignKey: InternalForeignKeyMetaData? = fkTableMetaData
            .getImportedForeignKey(fkName) as InternalForeignKeyMetaData?

          if (exportedForeignKey == null) {
            pkTableMetaData.addExportedForeignKey(ForeignKeyMetaDataImpl(pkTableMetaData, fkName, fkColumn, pkColumn))
          } else {
            exportedForeignKey.addColumnTuple(fkColumn, pkColumn)
          }

          if (importedForeignKey == null) {
            fkTableMetaData.addImportedForeignKey(ForeignKeyMetaDataImpl(fkTableMetaData, fkName, fkColumn, pkColumn))
          } else {
            importedForeignKey.addColumnTuple(fkColumn, pkColumn)
          }
        }
      }
    }
  }

  @Throws(SQLException::class)
  private fun updateTableWithIndexInformation(
    metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData,
    table: InternalTableMetaData
  ) {
    LOG.debug("Retrieving index information for " + table.tableName)

    val tableFilter: DatabaseTableFilter = connectorRepository.getConnectorHint(connectorId, DatabaseTableFilter::class.java)
      .value
    val resultSet = metaData.getIndexInfo(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), table.tableName, false,
      true
    )
    resultSet.use {
      while (resultSet.next()) {
        val nonUnique = resultSet.getBoolean("NON_UNIQUE")
        val columnName = resultSet.getString("COLUMN_NAME")
        val indexName = resultSet.getString("INDEX_NAME") ?: "IDX_UNKNOWN_$columnName"
        val ascOrDesc: String? = resultSet.getString("ASC_OR_DESC")

        if (columnName != null) {
          val column = table.getColumnMetaData(columnName)

          // May be strange SYS...$ column as with Oracle
          if (column != null) {
            var indexMetaData: InternalIndexMetaData? = table.getIndexMetaData(indexName) as InternalIndexMetaData?

            if (indexMetaData == null) {
              val ascending = ascOrDesc == null || "A" == ascOrDesc
              val unique = !nonUnique

              indexMetaData = IndexMetaDataImpl(table, indexName, ascending, unique, column.isPrimaryKey)
              table.addIndex(indexMetaData)
            }

            indexMetaData.addColumn(column)
          }
        }
      }
    }
  }

  @Throws(SQLException::class)
  private fun updateColumnsWithPrimaryKeyInformation(
    metaData: JdbcDatabaseMetaData,
    databaseMetaData: DatabaseMetaData,
    table: TableMetaData
  ) {
    LOG.debug("Retrieving primary key information for " + table.tableName)
    val tableFilter: DatabaseTableFilter = connectorRepository.getConnectorHint(connectorId, DatabaseTableFilter::class.java)
      .value
    val resultSet = metaData.getPrimaryKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), table.tableName
    )
    resultSet.use {
      while (resultSet.next()) {
        val pkName = resultSet.getString("PK_NAME")
        val columnName = resultSet.getString("COLUMN_NAME") ?: throw GuttenBaseException("COLUMN_NAME must not be null")

        if (pkName != null) {
          val columnMetaData: InternalColumnMetaData = table.getColumnMetaData(columnName) as InternalColumnMetaData?
            ?: throw IllegalStateException("No column meta data for $columnName")
          columnMetaData.isPrimaryKey = true
        }
      }
    }
  }

  @Throws(SQLException::class)
  private fun updateTableMetaDataWithColumnInformation(
    statement: Statement, tableMetaData: InternalTableMetaData,
    schemaPrefix: String
  ) {
    val tableName = escapeTableName(tableMetaData, schemaPrefix)
    val columnFilter: DatabaseColumnFilter = connectorRepository.getConnectorHint(connectorId, DatabaseColumnFilter::class.java)
      .value
    LOG.debug("Retrieving column information for $tableName")
    val selectSQL = SELECT_NOTHING_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)
    val resultSet = statement.executeQuery(selectSQL)

    resultSet.use {
      val meta: ResultSetMetaData = resultSet.metaData
      val columnCount: Int = meta.columnCount

      for (i in 1..columnCount) {
        val columnTypeName: String = meta.getColumnTypeName(i)
        val columnType: Int = meta.getColumnType(i)
        val columnName: String = meta.getColumnName(i)
        val columnClassName: String = meta.getColumnClassName(i)
        val isNullable = meta.isNullable(i) != ResultSetMetaData.columnNoNulls
        val isAutoIncrement: Boolean = meta.isAutoIncrement(i)
        val precision: Int = meta.getPrecision(i)
        val scale: Int = meta.getScale(i)
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
    connectorRepository.getConnectorHint(connectorId, SelectWhereClause::class.java)
      .value.getWhereClause(tableMetaData)

  @Throws(SQLException::class)
  private fun updateTableWithRowCount(statement: Statement, tableMetaData: InternalTableMetaData, schemaPrefix: String) {
    val filter: TableRowCountFilter =
      connectorRepository.getConnectorHint(connectorId, TableRowCountFilter::class.java).value

    if (filter.accept(tableMetaData)) {
      val tableName = escapeTableName(tableMetaData, schemaPrefix)
      LOG.debug("Retrieving row count for $tableName")

      val countAllSQL = SELECT_COUNT_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)
      val filterClause = createWhereClause(tableMetaData).trim { it <= ' ' }
      val countFilteredSQL = SELECT_COUNT_STATEMENT.replace(TABLE_PLACEHOLDER, tableName) + " " + filterClause
      val totalCount = getCount(statement, countAllSQL)
      val filteredCount = if ("" == filterClause) totalCount else getCount(statement, countFilteredSQL)

      tableMetaData.totalRowCount = totalCount
      tableMetaData.filteredRowCount = filteredCount
    } else {
      tableMetaData.totalRowCount = filter.defaultRowCount(tableMetaData)
      tableMetaData.filteredRowCount = filter.defaultRowCount(tableMetaData)
    }
  }

  @Throws(SQLException::class)
  private fun getCount(statement: Statement, countAllSQL: String): Int {
    val countResultSet = statement.executeQuery(countAllSQL)

    return countResultSet.use {
      countResultSet.next()
      countResultSet.getInt(1)
    }
  }

  @Throws(SQLException::class)
  private fun loadTables(databaseMetaData: InternalDatabaseMetaData, metaData: JdbcDatabaseMetaData) {
    LOG.debug("Searching tables in schema " + databaseMetaData.schema)

    val tableFilter = connectorRepository.getConnectorHint(connectorId, DatabaseTableFilter::class.java).value

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
        val tableCatalog: String? = resultSet.getString("TABLE_CAT")
        val tableSchema: String? = resultSet.getString("TABLE_SCHEM")
        val tableName: String = resultSet.getString("TABLE_NAME")
          ?: throw GuttenBaseException("TABLE_NAME must not be null in meta data result set")
        val tableType: String = resultSet.getString("TABLE_TYPE")
          ?: throw GuttenBaseException("TABLE_TYPE must not be null in meta data result set")

        LOG.debug("Found: $tableCatalog/$tableSchema/$tableName/$tableType")

        val tableMetaData = TableMetaDataImpl(databaseMetaData, tableName, tableType, tableCatalog, tableSchema)

        if (tableFilter.accept(tableMetaData)) {
          databaseMetaData.addTable(tableMetaData)
        }
      }

      LOG.info("Accepted tables: " + databaseMetaData.tableMetaData)
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(DatabaseMetaDataInspectorTool::class.java)

    private const val TABLE_PLACEHOLDER = "<table>"
    private const val SELECT_COUNT_STATEMENT = "SELECT COUNT(*) FROM $TABLE_PLACEHOLDER"
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

    private fun isPrimitive(clazz: Class<*>) = clazz != Void::class.java && (clazz.isPrimitive || clazz == String::class.java)

    private fun escapeTableName(tableMetaData: InternalTableMetaData, schemaPrefix: String): String {
      val tableName = schemaPrefix + tableMetaData.tableName

      return if (tableName.contains(" ")) {
        "\"$tableName\""
      } else {
        tableName
      }
    }
  }
}
