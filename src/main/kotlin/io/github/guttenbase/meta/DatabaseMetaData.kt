package io.github.guttenbase.meta

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import java.io.Serializable
import java.sql.JDBCType

/**
 * Information about data base such as schema name.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseMetaData : Serializable {
  val schema: String
  val schemaPrefix: String

  val databaseProperties: Map<String, Any>

  /**
   * Return tables list filtered by @see [io.github.guttenbase.repository.RepositoryTableFilter]
   */
  val tableMetaData: List<TableMetaData>
  fun getTableMetaData(tableName: String): TableMetaData?

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

data class DatabaseSupportedColumnType(
  val typeName: String,
  val jdbcType: JDBCType,
  val maxPrecision: Int = 0,
  val maxScale: Int = 0,
  val nullable: Boolean = true
) : Serializable, Comparable<DatabaseSupportedColumnType> {
  override fun compareTo(other: DatabaseSupportedColumnType) = typeName.compareTo(other.typeName)
}
