package io.github.guttenbase.connector

import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository

/**
 * Information about connectors, in particular, parameters needed to establish a [java.sql.Connection] to the data base.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ConnectorInfo {
  /**
   * @return Data base user
   */
  val user: String

  /**
   * @return Data base password
   */
  val password: String

  /**
   * @return Data base schema
   */
  val schema: String

  /**
   * @return Data base type
   */
  val databaseType: DatabaseType

  /**
   * Create connector
   */
  fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): Connector
}