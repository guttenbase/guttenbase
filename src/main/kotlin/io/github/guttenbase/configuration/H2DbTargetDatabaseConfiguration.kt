package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

/**
 * Implementation for H2DB data base.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class H2DbTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    setReferentialIntegrity(connection, false)
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, true)
  }

  private fun setReferentialIntegrity(connection: Connection, enable: Boolean) {
    val referentialIntegrity = if (enable) "TRUE" else "FALSE"
    executeSQL(connection, "SET REFERENTIAL_INTEGRITY $referentialIntegrity")
  }
}
