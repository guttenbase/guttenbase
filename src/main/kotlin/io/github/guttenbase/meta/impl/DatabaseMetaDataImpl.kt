package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import io.github.guttenbase.serialization.UUIDSerializer
import kotlinx.serialization.Polymorphic
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
@Serializable
class DatabaseMetaDataImpl(
  @kotlinx.serialization.Transient
  @Transient
  override var connectorRepository: ConnectorRepository = REPO_FOR_SERIALIZATION,
  override var connectorId: String,
  override val schema: String,

  override val databaseProperties: DatabasePropertiesType,
  override val databaseType: DatabaseType
) : InternalDatabaseMetaData {
  constructor(databaseMetaData: DatabaseMetaData) : this(
    databaseMetaData.connectorRepository,
    databaseMetaData.connectorId,
    databaseMetaData.schema,
    LinkedHashMap(databaseMetaData.databaseProperties),
    databaseMetaData.databaseType
  )

  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val syntheticId = UUID.randomUUID()!!

  @SerialName("tables")
  private val tableMap = LinkedHashMap<String, TableMetaData>()

  @SerialName("views")
  private val viewMap = LinkedHashMap<String, ViewMetaData>()

  @SerialName("supportedTypes")
  private val supportedTypeMap = mutableMapOf<JDBCType, MutableList<DatabaseSupportedColumnType>>()

  //
  // Derived values, not to be serialized
  //
  override val supportedTypes: Map<JDBCType, List<DatabaseSupportedColumnType>>
    get() = supportedTypeMap.entries.associate { entry -> entry.key to entry.value.toList() }

  override val allTypes: List<DatabaseSupportedColumnType>
    get() = supportedTypes.values.flatten().sorted()

  override val metaData: JdbcDatabaseMetaData
    get() = createMetaDataProxy()

  override val schemaPrefix get() = if (schema.isNotBlank()) "$schema." else ""

  override val tables get() = ArrayList(tableMap.values)

  override fun getTable(tableName: String) = tableMap[tableName.uppercase()]

  override fun addTable(tableMetaData: TableMetaData) {
    tableMap[tableMetaData.tableName.uppercase()] = tableMetaData
  }

  override fun removeTable(tableMetaData: TableMetaData) {
    tableMap.remove(tableMetaData.tableName.uppercase())
  }

  override val views get() = ArrayList(viewMap.values)

  override fun getView(viewName: String) = viewMap[viewName.uppercase()]

  override fun addView(viewMetaData: ViewMetaData) {
    viewMap[viewMetaData.tableName.uppercase()] = viewMetaData
  }

  override fun removeView(viewMetaData: ViewMetaData) {
    viewMap.remove(viewMetaData.tableName.uppercase())
  }

  override fun addSupportedType(type: String, jdbcType: JDBCType, precision: Int, scale: Int, nullable: Boolean) {
    val list = supportedTypeMap.computeIfAbsent(jdbcType) { mutableListOf<DatabaseSupportedColumnType>() }
    list.add(DatabaseSupportedColumnType(type.uppercase(), jdbcType, precision, scale, nullable))
  }

  override fun hashCode() = databaseType.hashCode() + schema.uppercase(Locale.getDefault()).hashCode()

  override fun equals(other: Any?) = other is DatabaseMetaData &&
    databaseType == other.databaseType && schema.equals(other.schema, ignoreCase = true)

  private fun createMetaDataProxy() = Proxy.newProxyInstance(
    javaClass.classLoader, arrayOf<Class<*>>(JdbcDatabaseMetaData::class.java)
  ) { _, method: Method, _ -> databaseProperties[method.name]?.value } as JdbcDatabaseMetaData
}

typealias ValueType = @Polymorphic PrimitiveValue<*>
typealias DatabasePropertiesType = Map<String, ValueType>
