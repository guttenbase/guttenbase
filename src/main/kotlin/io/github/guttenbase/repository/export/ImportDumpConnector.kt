package io.github.guttenbase.repository.export

import io.github.guttenbase.connector.impl.AbstractConnector
import io.github.guttenbase.exceptions.ImportException
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException

/**
 * Connection info for importing data from a file.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 *
 * Hint is used by [ImporterFactoryHint] to determine importer implementation
 *
 * @author M. Dahm
 */
class ImportDumpConnector(
  connectorRepository: ConnectorRepository,
  connectorId: String,
  private val importDumpConnectionInfo: ImportDumpConnectionInfo
) : AbstractConnector(connectorRepository, connectorId, importDumpConnectionInfo) {
  private lateinit var databaseMetaData: DatabaseMetaData

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun openConnection(): Connection {
    if (!connectionReady()) {
      try {
        val importer: Importer = connectorRepository.getConnectorHint(connectorId, ImporterFactory::class.java).value
          .createImporter()
        importer.initializeImport(_connectorRepository, _connectorId, importDumpConnectionInfo)
        databaseMetaData = importer.readDatabaseMetaData()
        _connection = ImportDumpConnection(importer, databaseMetaData)
      } catch (e: Exception) {
        throw ImportException("openConnection", e)
      }
    }
    return _connection
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  fun retrieveDatabaseMetaData(): DatabaseMetaData? {
    // Make sure the information is there
    openConnection()
    closeConnection()
    return databaseMetaData
  }
}
