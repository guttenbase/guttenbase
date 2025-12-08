package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.DatabaseTableFilter
import io.github.guttenbase.utils.Util

const val TABLE_TYPE = "TABLE"
const val VIEW_TYPE = "VIEW"

/**
 * Regard which tables when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for tables. The methods refer to
 * the parameters passed to JDBC data base meta data methods such as [DatabaseMetaData.getTable]
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
open class DefaultDatabaseTableFilter : DatabaseTableFilter {
  override fun getCatalog(databaseMetaData: DatabaseMetaData) = when (databaseMetaData.databaseType) {
    MARIADB, MYSQL -> databaseMetaData.schema
    else -> null
  }

  override fun getSchema(databaseMetaData: DatabaseMetaData) =
    if ("" == Util.trim(databaseMetaData.schema)) null else databaseMetaData.schema

  override fun getSchemaPattern(databaseMetaData: DatabaseMetaData) = when (databaseMetaData.databaseType) {
    MARIADB, MYSQL -> null
    else -> getSchema(databaseMetaData)
  }

  override fun getTableNamePattern(databaseMetaData: DatabaseMetaData) = "%"

  override fun getColumnNamePattern(databaseMetaData: DatabaseMetaData)= "%"

  override fun getTableTypes(databaseMetaData: DatabaseMetaData) = arrayOf(TABLE_TYPE, VIEW_TYPE)

  override  fun accept(table: DatabaseEntityMetaData) = when (table.database.databaseType) {
    POSTGRESQL -> !table.tableName.uppercase().startsWith("SQL_")
    H2DB -> table.tableSchema?.uppercase() == "PUBLIC"
    else -> true
  }
}
