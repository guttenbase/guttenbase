package io.github.guttenbase.meta

import io.github.guttenbase.meta.impl.DatabasePropertiesType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import kotlinx.serialization.Serializable
import java.sql.JDBCType

/**
 * Information about data base such as schema name.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseMetaData : java.io.Serializable, MetaData {
  val schema: String
  val schemaPrefix: String

  val databaseProperties: DatabasePropertiesType

  /**
   * Return tables list filtered by @see [io.github.guttenbase.repository.RepositoryTableFilter]
   */
  val tableMetaData: List<TableMetaData>
  fun getTable(tableName: String): TableMetaData?

  /**
   * @return (cached) meta data
   */
  val supportedTypes: Map<JDBCType, List<DatabaseSupportedColumnType>>
  val allTypes: List<DatabaseSupportedColumnType>
  val databaseMetaData: JdbcDatabaseMetaData
  val databaseType: DatabaseType
  val connectorRepository: ConnectorRepository
  val connectorId: String
}

const val PRECISION_PLACEHOLDER = "()"

@Serializable
data class DatabaseSupportedColumnType(
  val typeName: String,
  val jdbcType: JDBCType,
  val maxPrecision: Int = 0,
  val maxScale: Int = 0,
  val nullable: Boolean = true
) : java.io.Serializable, Comparable<DatabaseSupportedColumnType> {
  override fun compareTo(other: DatabaseSupportedColumnType) = typeName.compareTo(other.typeName)
}
