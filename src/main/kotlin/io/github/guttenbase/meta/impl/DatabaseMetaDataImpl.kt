package io.github.guttenbase.meta.impl

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.util.*

/**
 * Information about a data base/schema.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
class DatabaseMetaDataImpl(
  schema: String,
  override val databaseProperties: Map<String, Any>,
  override val databaseType: DatabaseType
) : InternalDatabaseMetaData {
  constructor(databaseMetaData: DatabaseMetaData) : this(
    databaseMetaData.schema,
    LinkedHashMap(databaseMetaData.databaseProperties),
    databaseMetaData.databaseType
  )

  override val schema: String = schema.trim { it <= ' ' }

  private val tableMetaDataMap: MutableMap<String, TableMetaData> = LinkedHashMap<String, TableMetaData>()

  override val databaseMetaData: JdbcDatabaseMetaData get() = createMetaDataProxy(databaseProperties)

  override val schemaPrefix: String get() = if (schema.isNotBlank()) "$schema." else ""

  override val tableMetaData: List<TableMetaData> get() = ArrayList(tableMetaDataMap.values)

  override fun getTableMetaData(tableName: String): TableMetaData? = tableMetaDataMap[tableName.uppercase()]

  override fun addTable(tableMetaData: TableMetaData) {
    tableMetaDataMap[tableMetaData.tableName.uppercase()] = tableMetaData
  }

  override fun removeTable(tableMetaData: TableMetaData) {
    tableMetaDataMap.remove(tableMetaData.tableName.uppercase())
  }

  override fun hashCode() = databaseType.hashCode() + schema.uppercase(Locale.getDefault()).hashCode()

  override fun equals(other: Any?): Boolean {
    val that = other as DatabaseMetaData
    return databaseType == that.databaseType && schema.equals(that.schema, ignoreCase = true)
  }

  private fun createMetaDataProxy(properties: Map<String, Any>): JdbcDatabaseMetaData {
    return Proxy.newProxyInstance(
      javaClass.classLoader, arrayOf<Class<*>>(JdbcDatabaseMetaData::class.java)
    ) { _: Any, method: Method, _: Array<Any?>? -> properties[method.name] } as JdbcDatabaseMetaData
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
