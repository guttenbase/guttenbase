package io.github.guttenbase.meta

import io.github.guttenbase.connector.DatabaseType
import java.io.Serializable
import java.sql.DatabaseMetaData

/**
 * Information about data base such as schema name.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseMetaData : Serializable {
  val schema: String
  val schemaPrefix: String

  /**
   * Return tables list filtered by @see [io.github.guttenbase.repository.RepositoryTableFilter]
   */
  val tableMetaData: List<TableMetaData>
  fun getTableMetaData(tableName: String): TableMetaData?

  /**
   * @return (cached) meta data
   */
  val databaseMetaData: DatabaseMetaData
  val databaseType: DatabaseType
}
