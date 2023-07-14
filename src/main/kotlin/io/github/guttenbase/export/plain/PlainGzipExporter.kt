package io.github.guttenbase.export.plain

import io.github.guttenbase.export.ExportDumpConnectorInfo
import io.github.guttenbase.export.ExportDumpExtraInformation
import io.github.guttenbase.export.ExportTableHeader
import io.github.guttenbase.export.Exporter
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.io.*
import java.sql.SQLException
import java.util.zip.GZIPOutputStream

/**
 * Export schema information and data into gzipped [ObjectOutputStream] file with serialized data.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class PlainGzipExporter : Exporter {
  private lateinit var objectOutputStream: ObjectOutputStream
  private lateinit var connectorRepository: ConnectorRepository
  private lateinit var connectorId: String
  private lateinit var exportDumpConnectionInfo: ExportDumpConnectorInfo

  /**
   * {@inheritDoc}
   */
  @Throws(Exception::class)
  override fun initializeExport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    exportDumpConnectionInfo: ExportDumpConnectorInfo
  ) {
    this.connectorRepository = connectorRepository
    this.connectorId = connectorId
    this.exportDumpConnectionInfo = exportDumpConnectionInfo

    openOutputStream(exportDumpConnectionInfo)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(Exception::class)
  override fun finishExport() {
    writeExtraInformation()
    objectOutputStream.close()
  }

  @Throws(IOException::class)
  override fun writeTableHeader(exportTableHeader: ExportTableHeader) {
    objectOutputStream.writeObject(exportTableHeader)
  }

  @Throws(IOException::class)
  override fun writeDatabaseMetaData(databaseMetaData: DatabaseMetaData) {
    objectOutputStream.writeObject(databaseMetaData)
  }

  override fun initializeWriteTableData(tableMetaData: TableMetaData) {}
  override fun finalizeWriteTableData(tableMetaData: TableMetaData) {}
  override fun initializeWriteRowData(tableMetaData: TableMetaData) {}
  override fun finalizeWriteRowData(tableMetaData: TableMetaData) {}

  /**
   * Resets the output stream which reduces memory foot print drastically. See [ObjectOutputStream.reset] for details.
   */
  @Throws(IOException::class)
  override fun flush() {
    objectOutputStream.reset()
    objectOutputStream.flush()
  }

  @Throws(IOException::class)
  override fun writeObject(obj: Any?) {
    objectOutputStream.writeObject(obj)
  }

  @Throws(IOException::class, SQLException::class)
  private fun writeExtraInformation() {
    val exportDumpExtraInformation =
      connectorRepository.getConnectorHint(connectorId, ExportDumpExtraInformation::class.java).value
    val extraInformation =
      exportDumpExtraInformation.getExtraInformation(connectorRepository, connectorId, exportDumpConnectionInfo)

    writeObject(extraInformation)
  }

  @Throws(IOException::class)
  private fun openOutputStream(exportDumpConnectionInfo: ExportDumpConnectorInfo) {
    val file = File(exportDumpConnectionInfo.path)
    val fileOutputStream = FileOutputStream(file)
    val gzipOutputStream = GZIPOutputStream(fileOutputStream)

    objectOutputStream = ObjectOutputStream(gzipOutputStream)
  }
}
