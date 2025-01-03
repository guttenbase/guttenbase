package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.DatabaseTableFilter
import io.github.guttenbase.utils.Util


/**
 * Regard which tables when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for tables. The methods refer to
 * the parameters passed to JDBC data base meta data methods such as [DatabaseMetaData.getTableMetaData]
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
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

  override fun getTableTypes(databaseMetaData: DatabaseMetaData) = arrayOf("TABLE")

  override fun accept(table: TableMetaData) = when (table.databaseMetaData.databaseType) {
    POSTGRESQL -> !table.tableName.uppercase().startsWith("SQL_")
    H2DB -> table.tableSchema?.uppercase() == "PUBLIC"
    else -> true
  }
}
