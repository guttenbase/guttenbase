package io.github.guttenbase.meta.impl

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.utils.Util
import io.github.guttenbase.utils.Util.immutable
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.DatabaseMetaData
import java.util.*

/**
 * Information about a data base/schema.
 *
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DatabaseMetaDataImpl(
  schema: String,
  databaseProperties: Map<String, Any>,
  override val databaseType: DatabaseType
) : InternalDatabaseMetaData {
  override val schema: String = schema.trim { it <= ' ' }
  private val _tableMetaDataMap: MutableMap<String, TableMetaData> = LinkedHashMap<String, TableMetaData>()

  @delegate:Transient
  override val databaseMetaData: DatabaseMetaData by lazy { createMetaDataProxy(databaseProperties) }

  override val schemaPrefix: String get() = if (schema.isNotBlank()) "$schema." else ""

  override val tableMetaData: List<TableMetaData> by immutable(_tableMetaDataMap.values)

  override fun getTableMetaData(tableName: String): TableMetaData? = _tableMetaDataMap[tableName.uppercase()]

  override fun addTableMetaData(tableMetaData: TableMetaData) {
    _tableMetaDataMap[tableMetaData.tableName.uppercase()] = tableMetaData
  }

  override fun removeTableMetaData(tableMetaData: TableMetaData) {
    _tableMetaDataMap.remove(tableMetaData.tableName.uppercase())
  }

  override fun hashCode() = databaseType.hashCode() + schema.uppercase(Locale.getDefault()).hashCode()

  override fun equals(other: Any?): Boolean {
    val that = other as io.github.guttenbase.meta.DatabaseMetaData
    return databaseType == that.databaseType && schema.equals(that.schema, ignoreCase = true)
  }

  private fun createMetaDataProxy(properties: Map<String, Any>): DatabaseMetaData {
    return Proxy.newProxyInstance(
      javaClass.classLoader, arrayOf<Class<*>>(DatabaseMetaData::class.java)
    ) { _: Any, method: Method, _: Array<Any?>? -> properties[method.name] } as DatabaseMetaData
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
