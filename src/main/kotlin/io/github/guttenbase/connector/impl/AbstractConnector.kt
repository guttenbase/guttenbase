package io.github.guttenbase.connector.impl

import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

/**
 * Default connector implementation.
 *
 * @author M. Dahm
 */
abstract class AbstractConnector(
  protected val connectorRepository: ConnectorRepository,
  protected val connectorId: String,
  protected val connectionInfo: ConnectorInfo
) : Connector {
  @Transient
  protected lateinit var connection: Connection

  /**
   * {@inheritDoc}
   */
  override fun closeConnection() {
    try {
      if (this::connection.isInitialized) {
        if (!connection.isClosed) {
          val targetDatabaseConfiguration: TargetDatabaseConfiguration =
            connectorRepository.getTargetDatabaseConfiguration(connectorId)

          if (!connection.autoCommit && targetDatabaseConfiguration.isMayCommit) {
            connection.commit()
          }

          connection.close()
        }
      }
    } catch (e: SQLException) {
      LOG.warn("Closing connection failed", e)
      throw e
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    val inspector = DatabaseMetaDataInspectorTool(connectorRepository, connectorId)
    val connection = openConnection()
    val sourceDatabaseConfiguration = connectorRepository.getSourceDatabaseConfiguration(connectorId)
    sourceDatabaseConfiguration.initializeSourceConnection(connection, connectorId)

    val databaseMetaData = inspector.getDatabaseMetaData(connection)
    sourceDatabaseConfiguration.finalizeSourceConnection(connection, connectorId)

    closeConnection()
    return databaseMetaData
  }

  protected fun connectionReady() = this::connection.isInitialized && !connection.isClosed

  companion object {
    @JvmStatic
    protected val LOG: Logger = LoggerFactory.getLogger(AbstractConnector::class.java)
  }
}