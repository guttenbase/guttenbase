package io.github.guttenbase.defaults.impl

import io.github.guttenbase.connector.DatabaseType.*
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.DatabaseTableFilter
import io.github.guttenbase.utils.Util


open class DefaultDatabaseTableFilter : DatabaseTableFilter {
  override fun getCatalog(databaseMetaData: DatabaseMetaData): String? {
    return when (databaseMetaData.databaseType) {
      MARIADB, MYSQL -> databaseMetaData.schema
      else -> null
    }
  }

  override fun getSchema(databaseMetaData: DatabaseMetaData): String? {
    return if ("" == Util.trim(databaseMetaData.schema)) null else databaseMetaData.schemaPrefix
  }

  override fun getSchemaPattern(databaseMetaData: DatabaseMetaData): String? {
    return when (databaseMetaData.databaseType) {
      MARIADB, MYSQL -> null
      else -> getSchema(databaseMetaData)
    }
  }

  override fun getTableNamePattern(databaseMetaData: DatabaseMetaData) = "%"

  override fun getTableTypes(databaseMetaData: DatabaseMetaData) = arrayOf("TABLE")

  override fun accept(table: TableMetaData): Boolean {
    return when (table.databaseMetaData.databaseType) {
      POSTGRESQL -> !table.tableName.uppercase().startsWith("SQL_")
      else -> true
    }
  }
}
