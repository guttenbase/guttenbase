package io.github.guttenbase.configuration

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException


/**
 * Implementation for HSQLDB data base.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class HsqldbTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    setReferentialIntegrity(connection, false, connectorRepository.getDatabaseMetaData(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, true, connectorRepository.getDatabaseMetaData(connectorId))
  }

  @Throws(SQLException::class)
  private fun setReferentialIntegrity(connection: Connection, enable: Boolean, databaseMetaData: DatabaseMetaData) {
    val databaseMajorVersion: Int = databaseMetaData.databaseMetaData.databaseMajorVersion
    val referentialIntegrity = if (enable) "TRUE" else "FALSE"
    val command = if (databaseMajorVersion < 2) "SET REFERENTIAL_INTEGRITY " else "SET DATABASE REFERENTIAL INTEGRITY "
    executeSQL(connection, command + referentialIntegrity)
  }
}
