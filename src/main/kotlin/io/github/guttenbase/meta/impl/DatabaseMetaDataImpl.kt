package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import java.sql.JDBCType
import java.util.*

/**
 * Information about a data base/schema.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
class DatabaseMetaDataImpl(
  @Transient
  override val connectorRepository: ConnectorRepository,
  override val connectorId: String,
  schema: String,
  override val databaseProperties: Map<String, Any>,
  override val databaseType: DatabaseType
) : InternalDatabaseMetaData {
  constructor(databaseMetaData: DatabaseMetaData) : this(
    databaseMetaData.connectorRepository,
    databaseMetaData.connectorId,
    databaseMetaData.schema,
    LinkedHashMap(databaseMetaData.databaseProperties),
    databaseMetaData.databaseType
  )

  override val supportedTypes: Map<JDBCType, List<DatabaseSupportedColumnType>>
    get() = supportedTypeMap.entries.associate { entry -> entry.key to entry.value.toList() }

  override val allTypes: List<DatabaseSupportedColumnType>
    get() = supportedTypes.values.flatten().sorted()

  override val schema = schema.trim { it <= ' ' }

  private val tableMetaDataMap = LinkedHashMap<String, TableMetaData>()

  private val supportedTypeMap = mutableMapOf<JDBCType, MutableList<DatabaseSupportedColumnType>>()

  override val databaseMetaData get() = createMetaDataProxy(databaseProperties)

  override val schemaPrefix get() = if (schema.isNotBlank()) "$schema." else ""

  override val tableMetaData get() = ArrayList(tableMetaDataMap.values)

  override fun getTableMetaData(tableName: String) = tableMetaDataMap[tableName.uppercase()]

  override fun addTable(tableMetaData: TableMetaData) {
    tableMetaDataMap[tableMetaData.tableName.uppercase()] = tableMetaData
  }

  override fun removeTable(tableMetaData: TableMetaData) {
    tableMetaDataMap.remove(tableMetaData.tableName.uppercase())
  }

  override fun addSupportedType(type: String, jdbcType: JDBCType, precision: Int, scale: Int, nullable: Boolean) {
    val list = supportedTypeMap.computeIfAbsent(jdbcType) { mutableListOf<DatabaseSupportedColumnType>() }
    list.add(DatabaseSupportedColumnType(type.uppercase(), jdbcType, precision, scale, nullable))
  }

  override fun hashCode() = databaseType.hashCode() + schema.uppercase(Locale.getDefault()).hashCode()

  override fun equals(other: Any?) = other is DatabaseMetaData &&
      databaseType == other.databaseType && schema.equals(other.schema, ignoreCase = true)

  private fun createMetaDataProxy(properties: Map<String, Any>): JdbcDatabaseMetaData {
    return Proxy.newProxyInstance(
      javaClass.classLoader, arrayOf<Class<*>>(JdbcDatabaseMetaData::class.java)
    ) { _: Any, method: Method, _: Array<Any?>? -> properties[method.name] } as JdbcDatabaseMetaData
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
