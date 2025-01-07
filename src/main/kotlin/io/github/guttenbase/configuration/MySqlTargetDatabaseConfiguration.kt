package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException

/**
 * Implementation for MYSQL data base.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class MySqlTargetDatabaseConfiguration
/**
 * @param connectorRepository
 * @param disableUniqueChecks disable unique checks, too. Not just foreign key constraints.
 */
@JvmOverloads
constructor(connectorRepository: ConnectorRepository, private val disableUniqueChecks: Boolean = false) :
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

    if (disableUniqueChecks) {
      setUniqueChecks(connection, false)
    }
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, true)
    if (disableUniqueChecks) {
      setUniqueChecks(connection, true)
    }
  }

  @Throws(SQLException::class)
  private fun setReferentialIntegrity(connection: Connection, enable: Boolean) {
    executeSQL(connection, "SET FOREIGN_KEY_CHECKS = " + (if (enable) "1" else "0") + ";")
  }

  @Throws(SQLException::class)
  private fun setUniqueChecks(connection: Connection, enable: Boolean) {
    executeSQL(connection, "SET UNIQUE_CHECKS = " + (if (enable) "1" else "0") + ";")
  }
}
