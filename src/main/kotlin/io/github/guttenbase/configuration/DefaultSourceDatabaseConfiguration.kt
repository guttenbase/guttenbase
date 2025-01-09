package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

/**
 * (Almost) empty implementation
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  AbstractDatabaseConfiguration(connectorRepository), SourceDatabaseConfiguration {
  /**
   * Connection is set read only and autocommit is false.
   *
   * {@inheritDoc}
   */
  override fun initializeSourceConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }
    if (!connection.isReadOnly) {
      connection.isReadOnly = true
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeSourceConnection(connection: Connection, connectorId: String) {
  }

  /**
   * {@inheritDoc}
   */
  override fun beforeSelect(connection: Connection, connectorId: String, table: TableMetaData) {}

  /**
   * {@inheritDoc}
   */
  override fun afterSelect(connection: Connection, connectorId: String, table: TableMetaData) {}
}
