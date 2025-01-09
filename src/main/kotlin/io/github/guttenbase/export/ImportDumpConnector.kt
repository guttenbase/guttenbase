package io.github.guttenbase.export

import io.github.guttenbase.connector.impl.AbstractConnector
import io.github.guttenbase.exceptions.ImportException
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection

/**
 * Connection info for importing data from a file.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
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
  override fun openConnection(): Connection {
    if (!connectionReady()) {
      try {
        val importer = connectorRepository.hint<ImporterFactory>(connectorId).createImporter()
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
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    // Make sure the information is there
    openConnection()
    closeConnection()

    return databaseMetaData
  }
}
