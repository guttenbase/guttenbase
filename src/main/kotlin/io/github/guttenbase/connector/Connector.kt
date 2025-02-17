package io.github.guttenbase.connector

import io.github.guttenbase.meta.DatabaseMetaData
import java.sql.Connection

/**
 * Connectors maintain informations about the data base and how open and close the SQL [Connection]s.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface Connector : AutoCloseable {
  /**
   * Open connection or return existing connection
   */
  fun openConnection(): Connection

  /**
   * Close connection if it exists and is open
   */
  fun closeConnection()

  /**
   * Return information about database and tables
   */
  fun retrieveDatabase(): DatabaseMetaData

  override fun close() {
    closeConnection()
  }
}
