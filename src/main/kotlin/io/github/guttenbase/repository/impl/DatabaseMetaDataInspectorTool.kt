package io.github.guttenbase.repository.impl

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.meta.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.impl.*
import io.github.guttenbase.repository.*
import io.github.guttenbase.tools.SelectWhereClause
import io.github.guttenbase.utils.Util
import io.github.guttenbase.utils.Util.RIGHT_ARROW
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
      for (table in databaseMetaData.tables) {
        val tableMetaData = table as InternalTableMetaData

        retrieveColumns(statement, tableMetaData, schemaPrefix)
      }
    }

    try {
      for (table in databaseMetaData.tables) {
        val tableMetaData = table as InternalTableMetaData

        updateColumnsTypeInformation(metaData, databaseMetaData, tableMetaData)
        updateColumnsWithPrimaryKeyInformation(metaData, databaseMetaData, tableMetaData)
        updateColumnsWithForeignKeyInformation(metaData, databaseMetaData, tableMetaData)
        updateTableWithIndexInformation(metaData, databaseMetaData, tableMetaData)
      }
    } catch (e: Exception) {
      LOG.warn("Could not update additional schema information", e)
    }

    connection.createStatement().use { statement ->
      for (table in databaseMetaData.tables) {
        val tableMetaData = table as InternalTableMetaData

        enrichTableMetaData(statement, tableMetaData, schemaPrefix)
      }
    }
  }

  private fun updateColumnsWithForeignKeyInformation(
    metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData, table: TableMetaData
  ) {
    val tableName = table.tableName()

    LOG.debug("Retrieving foreign key information for $tableName")

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val fkFilter = connectorRepository.hint<DatabaseForeignKeyFilter>(connectorId)
    val resultSet = metaData.getExportedKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchemaPattern(databaseMetaData),
      tableName
    )

    resultSet.use {
      while (resultSet.next()) {
        val pkTableName = resultSet.getStringNotNull("PKTABLE_NAME")
        val pkColumnName = resultSet.getStringNotNull("PKCOLUMN_NAME")
        val fkTableName = resultSet.getStringNotNull("FKTABLE_NAME")
        val fkColumnName = resultSet.getStringNotNull("FKCOLUMN_NAME")
        val fkName = resultSet.getString("FK_NAME") ?: "${SYNTHETIC_CONSTRAINT_PREFIX}UNKNOWN_$fkColumnName"
        val pkTableMetaData = databaseMetaData.getTable(pkTableName) as InternalTableMetaData?
        val fkTableMetaData = databaseMetaData.getTable(fkTableName) as InternalTableMetaData?

        if (fkTableMetaData == null || pkTableMetaData == null) {
          // this table might have been excluded from the list of tables handled by this batch
          LOG.warn("Unable to retrieve metadata information for table $fkTableName referenced by $pkTableName")
        } else {
          val pkColumn = pkTableMetaData.getColumn(pkColumnName)!!
          val fkColumn = fkTableMetaData.getColumn(fkColumnName)!!
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
    val tableName = table.tableName()

    LOG.debug("Retrieving index information for $tableName")

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val indexFilter = connectorRepository.hint<DatabaseIndexFilter>(connectorId)
    val resultSet = metaData.getIndexInfo(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), tableName, false,
      true
    )

    resultSet.use {
      while (resultSet.next()) {
        val nonUnique = resultSet.getBoolean("NON_UNIQUE")
        val columnName = resultSet.getString("COLUMN_NAME")
        val indexName = resultSet.getString("INDEX_NAME") ?: "${SYNTHETIC_INDEX_PREFIX}UNKNOWN_$columnName"
        val ascOrDesc = resultSet.getString("ASC_OR_DESC")

        if (columnName != null) {
          val column = table.getColumn(columnName)

          if (column != null && !table.hasForeignKey(indexName)) {
            var indexMetaData = table.getIndex(indexName) as InternalIndexMetaData?

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

  private fun updateColumnsWithPrimaryKeyInformation(
    metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData, table: TableMetaData
  ) {
    val tableName = table.tableName()

    LOG.debug("Retrieving primary key information for {}", table)

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val resultSet = metaData.getPrimaryKeys(
      tableFilter.getCatalog(databaseMetaData),
      tableFilter.getSchema(databaseMetaData), tableName
    )

    resultSet.use {
      while (resultSet.next()) {
        val pkName = resultSet.getString("PK_NAME")
        val columnName =
          resultSet.getString("COLUMN_NAME") ?: throw GuttenBaseException("COLUMN_NAME must not be null")

        if (pkName != null) {
          val columnMetaData = table.getColumn(columnName) as InternalColumnMetaData?
            ?: throw IllegalStateException("No column meta data for $columnName")
          columnMetaData.isPrimaryKey = true
        }
      }
    }
  }

  private fun TableMetaData.tableName() = // && databaseType == DatabaseType.ORACLE
    if (tableName.contains(" ")) databaseType.escapeDatabaseEntity(tableName) else tableName

  private fun updateColumnsTypeInformation(
    metaData: JdbcDatabaseMetaData, databaseMetaData: DatabaseMetaData, table: TableMetaData
  ) {
    val tableName = table.tableName()

    LOG.debug("Retrieving column type information for $tableName")

    val tableFilter = connectorRepository.hint<DatabaseTableFilter>(connectorId)
    val resultSet = metaData.getColumns(
      tableFilter.getCatalog(databaseMetaData), tableFilter.getSchema(databaseMetaData),
      tableName, tableFilter.getColumnNamePattern(databaseMetaData)
    )

    resultSet.use {
      while (resultSet.next()) {
        val columnName = resultSet.getString("COLUMN_NAME") ?: throw GuttenBaseException("COLUMN_NAME must not be null")
        val columnSize = resultSet.getInt("COLUMN_SIZE")
        val defaultValue = resultSet.getString("COLUMN_DEF")
        val generated = resultSet.getString("IS_GENERATEDCOLUMN") == "YES"
        val columnMetaData = table.getColumn(columnName) as InternalColumnMetaData?

        if (columnMetaData != null) {
          columnMetaData.isGenerated = generated
          columnMetaData.columnSize = columnSize
          columnMetaData.defaultValue = defaultValue
        }
      }
    }
  }

  private fun retrieveColumns(statement: Statement, tableMetaData: InternalTableMetaData, schemaPrefix: String) {
    val databaseType = tableMetaData.databaseType
    val tableName = databaseType.escapeDatabaseEntity(tableMetaData.tableName, schemaPrefix)
    val columnFilter = connectorRepository.hint<DatabaseColumnFilter>(connectorId)
    val selectSQL = SELECT_NOTHING_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)

    LOG.debug("Retrieving column information for $tableName")

    val resultSet = statement.executeQuery(selectSQL)

    resultSet.use {
      val meta = resultSet.metaData
      val columnCount = meta.columnCount

      for (i in 1..columnCount) {
        val columnTypeName = meta.getColumnTypeName(i).uppercase()
        val columnType = meta.getColumnType(i)
        val columnName = meta.getColumnName(i)
        val columnClassName = meta.getColumnClassName(i)
        val isNullable = meta.isNullable(i) != ResultSetMetaData.columnNoNulls
        val isAutoIncrement = meta.isAutoIncrement(i)
        val precision = meta.getPrecision(i)
        val scale = meta.getScale(i)
        val column = ColumnMetaDataImpl(
          tableMetaData,
          columnType, columnName, columnTypeName, columnClassName,
          isNullable, isAutoIncrement, precision, scale
        )

        if (columnFilter.accept(column)) {
          tableMetaData.addColumn(column)
        }
      }
    }
  }

  private fun createWhereClause(tableMetaData: TableMetaData) =
    connectorRepository.hint<SelectWhereClause>(connectorId).getWhereClause(tableMetaData)

  private fun enrichTableMetaData(statement: Statement, table: InternalTableMetaData, schemaPrefix: String) {
    val filter = connectorRepository.hint<TableRowCountFilter>(connectorId)

    if (filter.accept(table)) {
      val databaseType = table.databaseType
      val tableName = databaseType.escapeDatabaseEntity(table.tableName, schemaPrefix)
      LOG.debug("Retrieving row count for $tableName")

      computeRowCount(tableName, table, statement)

      val primaryKeyColumn = table.getNumericPrimaryKeyColumn()

      if (primaryKeyColumn != null) {
        val maxIdStatement = SELECT_MIN_MAX_ID_STATEMENT.replace(TABLE_PLACEHOLDER, tableName)
          .replace(COLUMN_PLACEHOLDER, databaseType.escapeDatabaseEntity(primaryKeyColumn.columnName))

        statement.executeQuery(maxIdStatement).use {
          it.next()
          table.minId = it.getLong(1)
          table.maxId = it.getLong(2)
        }
      }
    } else {
      table.totalRowCount = filter.defaultRowCount(table)
      table.filteredRowCount = filter.defaultRowCount(table)
      table.minId = filter.defaultMinId(table)
      table.maxId = filter.defaultMaxId(table)
    }
  }

  private fun computeRowCount(tableName: String, tableMetaData: InternalTableMetaData, statement: Statement) {
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
        val prec = resultSet.getInt("PRECISION")
        val scale = resultSet.getInt("MAXIMUM_SCALE")
        val nullable = resultSet.getBoolean("NULLABLE")

        // "precision" gives you only a hint what the type really may hold, unfortunately
        // You can only make an educated guess later on
        val precision = if (jdbcType == JDBCType.CHAR && prec == 0) {
          LOG.warn("Fixing wrong precision for CHAR type on ${databaseMetaData.databaseType}")
          255
        } else {
          prec
        }

        databaseMetaData.addSupportedType(typeName.uppercase(), jdbcType, precision, scale, nullable)
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

      LOG.info("${databaseMetaData.tables.size} accepted tables: ${databaseMetaData.tables}")
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

    private fun getValue(method: Method, data: JdbcDatabaseMetaData): Pair<String, PrimitiveValue<*>>? {
      val key = method.name

      try {
        val value = method.invoke(data)

        if (value != null) {
          return key to value.toPrimitiveValue()
        }
      } catch (e: Exception) {
        LOG.warn("Could not get meta data property: $key $RIGHT_ARROW ${e.message}")
      }

      return null
    }

    private fun isPrimitive(clazz: Class<*>) =
      clazz != Void::class.java && (clazz.isPrimitive || clazz == String::class.java)
  }
}

private fun ResultSet.getStringNotNull(name: String): String = getString(name)
  ?: throw GuttenBaseException("Column $name must not be NULL")
