package io.github.guttenbase.repository

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.hints.ConnectorHint
import io.github.guttenbase.meta.DatabaseMetaData
import java.io.Serializable

/**
 * The main repository containing all configured connectors.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ConnectorRepository : Serializable {
  /**
   * Add connection info to repository with symbolic ID for data base such as "source db", e.g.
   */
  fun addConnectionInfo(connectorId: String, connectionInfo: ConnectorInfo)

  /**
   * Remove all information about connector
   */
  fun removeConnectionInfo(connectorId: String)

  /**
   * Get connection info
   */
  fun getConnectionInfo(connectorId: String): ConnectorInfo

  /**
   * Get all meta data from data base.
   */
  fun getDatabaseMetaData(connectorId: String): DatabaseMetaData

  /**
   * Reset table data, i.e. it will be reread from the data base.
   */
  fun refreshDatabaseMetaData(connectorId: String)

  /**
   * Create connector
   */
  fun createConnector(connectorId: String): Connector

  /**
   * Get configuration.
   */
  fun getSourceDatabaseConfiguration(connectorId: String): SourceDatabaseConfiguration

  /**
   * Get configuration.
   */
  fun getTargetDatabaseConfiguration(connectorId: String): TargetDatabaseConfiguration

  /**
   * Add configuration hint for connector.
   */
  fun <T : Any> addConnectorHint(connectorId: String, hint: ConnectorHint<T>)

  /**
   * Remove configuration hint for connector.
   */
  fun <T : Any> removeConnectorHint(connectorId: String, connectionInfoHintType: Class<T>)

  /**
   * Get configuration hint for connector.
   */
  fun <T : Any> getConnectorHint(connectorId: String, connectorHintType: Class<T>): ConnectorHint<T>

  /**
   * Get all currently configured connector IDs.
   */
  val connectorIds: List<String>

  /**
   * Define configuration for given data base type when reading data.
   */
  fun addSourceDatabaseConfiguration(databaseType: DatabaseType, sourceDatabaseConfiguration: SourceDatabaseConfiguration)

  /**
   * Define configuration for given data base type when writing data.
   */
  fun addTargetDatabaseConfiguration(databaseType: DatabaseType, targetDatabaseConfiguration: TargetDatabaseConfiguration)
}

typealias JdbcDatabaseMetaData = java.sql.DatabaseMetaData
