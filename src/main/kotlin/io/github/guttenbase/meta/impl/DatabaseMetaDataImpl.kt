package io.github.guttenbase.meta.impl

import io.github.guttenbase.connector.DatabaseType
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
 *  &copy; 2012-2034 akquinet tech@spree
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

  override val supportedTypes: List<DatabaseColumnType> get() = supportedTypeMap.values.flatten()

  override val schema = schema.trim { it <= ' ' }

  private val tableMetaDataMap = LinkedHashMap<String, TableMetaData>()

  private val supportedTypeMap = mutableMapOf<JDBCType, MutableList<DatabaseColumnType>>()

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
    val list = supportedTypeMap.computeIfAbsent(jdbcType) { mutableListOf<DatabaseColumnType>() }
    list.add(DatabaseColumnType(type.uppercase(), jdbcType, precision, scale, nullable))
  }

  override fun typeFor(columnMetaData: ColumnMetaData): DatabaseColumnType? {
    val possibleTypes = supportedTypeMap[columnMetaData.jdbcColumnType] ?: listOf<DatabaseColumnType>()

    // Prefer matching names, because the list may not be properly sorted (MSSQL 🙄)
    return possibleTypes.firstOrNull { it.realTypeName() == columnMetaData.realTypeName() }
      ?: possibleTypes.maxByOrNull { it.maxPrecision }
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

private fun String.realTypeName() = when (this.uppercase()) {
  "VARCHAR2" -> "VARCHAR"
  "CHARACTER" -> "CHAR"
  "CHARACTER VARYING" -> "VARCHAR"
  else -> this
}

private fun DatabaseColumnType.realTypeName() = typeName.realTypeName()

private fun ColumnMetaData.realTypeName() = columnTypeName.realTypeName()
