package io.github.guttenbase.export.gzip

import io.github.guttenbase.export.ExportTableHeader
import io.github.guttenbase.export.ImportDumpConnectionInfo
import io.github.guttenbase.export.ImportDumpExtraInformation
import io.github.guttenbase.export.Importer
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.util.zip.GZIPInputStream

/**
 * Import schema information and data from gzipped [ObjectInputStream] file with serialized data.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("unused")
class PlainGzipImporter : Importer {
  private lateinit var objectInputStream: ObjectInputStream
  private lateinit var connectorRepository: ConnectorRepository
  private lateinit var connectorId: String

  // Ensure that table data has been read when seeking the extra informations
  private var _tableDataRead = false

  @Throws(IOException::class)
  override fun initializeImport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    importDumpConnectionInfo: ImportDumpConnectionInfo
  ) {
    this.connectorRepository = connectorRepository
    this.connectorId = connectorId
    val gzipInputStream = GZIPInputStream(importDumpConnectionInfo.path.openStream())

    objectInputStream = ObjectInputStream(gzipInputStream)
  }

  @Throws(Exception::class)
  override fun finishImport() {
    if (_tableDataRead) { // At end
      readExtraInformation()
    }

    objectInputStream.close()
  }

  @Throws(Exception::class)
  override fun readDatabaseMetaData() = objectInputStream.readObject() as DatabaseMetaData

  @Throws(Exception::class)
  override fun seekTableHeader(tableMetaData: TableMetaData) {
    _tableDataRead = true

    var exportTableHeader: ExportTableHeader

    do {
      exportTableHeader = seekNextTableHeader()
    } while (!tableMetaData.tableName.equals(exportTableHeader.tableName, ignoreCase = true))
  }

  @Throws(Exception::class)
  override fun readObject(): Any? = objectInputStream.readObject()

  @Throws(Exception::class)
  private fun seekNextTableHeader(): ExportTableHeader {
    var value: Any

    do {
      value = objectInputStream.readObject()
    } while (value !is ExportTableHeader)

    return value
  }

  @Suppress("UNCHECKED_CAST")
  @Throws(Exception::class)
  private fun readExtraInformation() {
    val importDumpExtraInformation = connectorRepository.hint<ImportDumpExtraInformation>(connectorId)
    val extraInformation = readObject() as Map<String, Serializable>
    importDumpExtraInformation.processExtraInformation(extraInformation)
  }
}
