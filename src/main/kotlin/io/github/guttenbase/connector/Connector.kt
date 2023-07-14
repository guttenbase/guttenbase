package io.github.guttenbase.connector

import io.github.guttenbase.meta.DatabaseMetaData
import java.sql.Connection
import java.sql.SQLException

/**
 * Connectors maintain informations about the data base and how open and close the SQL [Connection]s.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface Connector : AutoCloseable {
  /**
   * Open connection or return existing connection
   */
  @Throws(SQLException::class)
  fun openConnection(): Connection

  /**
   * Close connection if it exists and is open
   */
  @Throws(SQLException::class)
  fun closeConnection()

  /**
   * Return information about database and tables
   */
  @Throws(SQLException::class)
  fun retrieveDatabaseMetaData(): DatabaseMetaData

  @Throws(SQLException::class)
  override fun close() {
    closeConnection()
  }
}
