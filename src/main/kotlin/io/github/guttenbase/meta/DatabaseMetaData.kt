package io.github.guttenbase.meta

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import java.io.Serializable
import java.sql.JDBCType

/**
 * Information about data base such as schema name.
 *
 *  &copy; 2012-2034 akquinet tech@spree
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
  val supportedTypes: List<DatabaseSupportedType>
  val databaseMetaData: JdbcDatabaseMetaData
  val databaseType: DatabaseType
  val connectorRepository: ConnectorRepository
  val connectorId: String

  fun typeFor(columnMetaData: ColumnMetaData): DatabaseSupportedType?
}

data class DatabaseSupportedType(
  val typeName: String, val jdbcType: JDBCType, val precision: Int, val nullable: Boolean
) : Serializable {
  val mayUsePrecision: Boolean
    get() = when (jdbcType) {
      JDBCType.CHAR, JDBCType.NCHAR, JDBCType.LONGVARCHAR, JDBCType.LONGNVARCHAR, JDBCType.LONGVARBINARY,
      JDBCType.VARCHAR, JDBCType.VARBINARY, JDBCType.NVARCHAR,
      JDBCType.BLOB, JDBCType.CLOB, JDBCType.NCLOB,

      JDBCType.DOUBLE, JDBCType.FLOAT, JDBCType.NUMERIC, JDBCType.DECIMAL, JDBCType.REAL -> precision > 0

      else -> false
    }
}