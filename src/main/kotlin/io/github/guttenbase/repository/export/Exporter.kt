package io.github.guttenbase.repository.export

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository

/**
 * Export schema information and table data to some custom format.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface Exporter {
  /**
   * Start exporting to a file.
   */
  @Throws(Exception::class)
  fun initializeExport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    exportDumpConnectionInfo: ExportDumpConnectorInfo
  )

  /**
   * Finish export
   */
  @Throws(Exception::class)
  fun finishExport()

  /**
   * Write table header when executing an INSERT statement. This is necessary to mark where data for a given table starts since some tables
   * may be skipped during import. The header is written only once in fact.
   */
  @Throws(Exception::class)
  fun writeTableHeader(exportTableHeader: ExportTableHeader)

  /**
   * Dump database information
   */
  @Throws(Exception::class)
  fun writeDatabaseMetaData(sourceDatabaseMetaData: DatabaseMetaData)

  /**
   * Called before copying of a table starts.
   */
  @Throws(Exception::class)
  fun initializeWriteTableData(tableMetaData: TableMetaData)

  /**
   * Called after copying of a table ends.
   */
  @Throws(Exception::class)
  fun finalizeWriteTableData(tableMetaData: TableMetaData)

  /**
   * Called before copying of a table row starts.
   */
  @Throws(Exception::class)
  fun initializeWriteRowData(tableMetaData: TableMetaData)

  /**
   * Called after copying of a table row ends.
   */
  @Throws(Exception::class)
  fun finalizeWriteRowData(tableMetaData: TableMetaData)

  /**
   * Allow the implementation to flush its buffers. This method is called by [ExportDumpConnection.commit].
   */
  @Throws(Exception::class)
  fun flush()

  @Throws(Exception::class)
  fun writeObject(obj: Any?)
}
