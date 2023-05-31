package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException

/**
 * Implementation for H2DB data base.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class H2DbTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    setReferentialIntegrity(connection, false)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, true)
  }

  @Throws(SQLException::class)
  private fun setReferentialIntegrity(connection: Connection, enable: Boolean) {
    val referentialIntegrity = if (enable) "TRUE" else "FALSE"
    executeSQL(connection, "SET REFERENTIAL_INTEGRITY $referentialIntegrity")
  }
}
