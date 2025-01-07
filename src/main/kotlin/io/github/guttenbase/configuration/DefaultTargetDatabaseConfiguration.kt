package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException

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
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun beforeInsert(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun beforeNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun afterNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
  }
}
