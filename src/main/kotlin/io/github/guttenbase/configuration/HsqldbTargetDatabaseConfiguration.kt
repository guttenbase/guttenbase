package io.github.guttenbase.configuration

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection


/**
 * Implementation for HSQLDB data base.
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
open class HsqldbTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    setReferentialIntegrity(connection, false, connectorRepository.getDatabase(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, true, connectorRepository.getDatabase(connectorId))
  }

  private fun setReferentialIntegrity(connection: Connection, enable: Boolean, databaseMetaData: DatabaseMetaData) {
    val databaseMajorVersion: Int = databaseMetaData.metaData.databaseMajorVersion
    val referentialIntegrity = if (enable) "TRUE" else "FALSE"
    val command = if (databaseMajorVersion < 2) "SET REFERENTIAL_INTEGRITY " else "SET DATABASE REFERENTIAL INTEGRITY "
    executeSQL(connection, command + referentialIntegrity)
  }
}
