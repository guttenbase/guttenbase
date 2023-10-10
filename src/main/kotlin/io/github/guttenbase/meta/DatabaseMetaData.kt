package io.github.guttenbase.meta

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.JdbcDatabaseMetaData
import java.io.Serializable

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
  val databaseMetaData: JdbcDatabaseMetaData
  val databaseType: DatabaseType
  val connectorRepository: ConnectorRepository
  val connectorId: String
}
