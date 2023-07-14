package io.github.guttenbase.export

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
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 *
 * Hint is used by [io.github.guttenbase.export.zip.ImporterFactoryHint] to determine importer implementation
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
        val importer = connectorRepository.getConnectorHint(connectorId, ImporterFactory::class.java).value
          .createImporter()
        importer.initializeImport(connectorRepository, connectorId, importDumpConnectionInfo)
        databaseMetaData = importer.readDatabaseMetaData()
        connection = ImportDumpConnection(importer, databaseMetaData)
      } catch (e: Exception) {
        throw ImportException("openConnection", e)
      }
    }

    return connection
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    // Make sure the information is there
    openConnection()
    closeConnection()

    return databaseMetaData
  }
}
