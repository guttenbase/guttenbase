package io.github.guttenbase.export

import io.github.guttenbase.connector.impl.AbstractConnector
import io.github.guttenbase.exceptions.ExportException
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import java.sql.Connection
import java.sql.SQLException

/**
 * Connection info for exporting database contents to a file.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.export.zip.ExporterFactoryHint] to determine exporter implementation
 *
 * @author M. Dahm
 */
class ExportDumpConnector(
  connectorRepository: ConnectorRepository, connectorId: String,
  private val exportDumpConnectionInfo: ExportDumpConnectorInfo
) : AbstractConnector(connectorRepository, connectorId, exportDumpConnectionInfo) {
  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun openConnection(): Connection {
    if (!connectionReady()) {
      try {
        val exporter =
          connectorRepository.getConnectorHint(connectorId, ExporterFactory::class.java).value.createExporter()
        exporter.initializeExport(connectorRepository, connectorId, exportDumpConnectionInfo)
        exporter.writeDatabaseMetaData(retrieveSourceDatabaseMetaData())
        connection = ExportDumpConnection(exporter)
      } catch (e: Exception) {
        throw ExportException("openConnection", e)
      }
    }

    return connection
  }

  /**
   * Table metadata is the same as the metadata of the source connector. The only difference is that the row count of all tables is reset
   * to 0.
   *
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    val data = retrieveSourceDatabaseMetaData()
    val result = Util.copyObject(DatabaseMetaData::class.java, data)

    for (tableMetaData in result.tableMetaData) {
      (tableMetaData as InternalTableMetaData).totalRowCount = 0
    }

    return result
  }

  @Throws(SQLException::class)
  private fun retrieveSourceDatabaseMetaData() =
    connectorRepository.getDatabaseMetaData(exportDumpConnectionInfo.sourceConnectorId)
}
