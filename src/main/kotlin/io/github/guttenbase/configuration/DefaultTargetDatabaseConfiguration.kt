package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

/**
 * (Almost) empty implementation
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  AbstractDatabaseConfiguration(connectorRepository), TargetDatabaseConfiguration {
  override val isMayCommit: Boolean
    /**
     * {@inheritDoc}
     *
     *
     * true by default
     */
    get() = true

  /**
   * Connection is set autocommit false.
   *
   *
   * {@inheritDoc}
   */
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
  }

  /**
   * {@inheritDoc}
   */
  override fun beforeInsert(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  override fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  override fun beforeNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  override fun afterNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
  }
}
