package io.github.guttenbase.export.zip


import io.github.guttenbase.export.ImportDumpConnectionInfo
import io.github.guttenbase.export.ImportDumpExtraInformation
import io.github.guttenbase.export.Importer
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import org.apache.commons.io.IOUtils
import java.io.*
import java.net.URL
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Read database information and data from ZIP file.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipImporter : Importer {
  private lateinit var zipFile: ZipFile
  private lateinit var objectInputStream: ObjectInputStream
  private lateinit var connectorRepository: ConnectorRepository
  private lateinit var connectorId: String

  @Throws(Exception::class)
  override fun initializeImport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    importDumpConnectionInfo: ImportDumpConnectionInfo
  ) {
    val url: URL = importDumpConnectionInfo.path
    var file = File(url.path)

    // In case it's an HTTP-URL or whatever, dump it to a file first
    if (!file.exists()) {
      file = File.createTempFile("GuttenBase", ".jar")
      file.deleteOnExit()

      val inputStream = url.openStream()
      val outputStream = FileOutputStream(file)

      IOUtils.copy(inputStream, outputStream)
      IOUtils.closeQuietly(inputStream)
      IOUtils.closeQuietly(outputStream)
    }

    this.connectorRepository = connectorRepository
    this.connectorId = connectorId
    zipFile = ZipFile(file)
  }

  @Throws(Exception::class)
  override fun finishImport() {
    zipFile.close()
  }

  @Throws(Exception::class)
  override fun readDatabaseMetaData(): DatabaseMetaData {
    val zipEntry: ZipEntry = zipFile.getEntry(ZipConstants.META_DATA) ?: error(
      "zipEntry != null"
    )
    val inputStream: InputStream = zipFile.getInputStream(zipEntry)
    val objectInputStream = ObjectInputStream(inputStream)
    val databaseMetaData = objectInputStream.readObject() as DatabaseMetaData

    objectInputStream.close()
    readExtraInformation()

    return databaseMetaData
  }

  @Throws(Exception::class)
  override fun readObject(): Any? = objectInputStream.readObject()

  @Throws(Exception::class)
  override fun seekTableHeader(tableMetaData: TableMetaData) {
    if (this::objectInputStream.isInitialized) {
      objectInputStream.close()
    }

    val zipEntry: ZipEntry = zipFile.getEntry(
      (ZipConstants.PREFIX + tableMetaData.tableName + ZipConstants.PATH_SEPARATOR) + ZipConstants.TABLE_DATA_NAME
    ) ?: error("zipEntry != null")
    objectInputStream = ObjectInputStream(zipFile.getInputStream(zipEntry))
  }

  @Throws(Exception::class)
  private fun readExtraInformation() {
    val importDumpExtraInformation: ImportDumpExtraInformation = connectorRepository.getConnectorHint(
      connectorId,
      ImportDumpExtraInformation::class.java
    ).value
    val extraInformation = HashMap<String, Serializable>()
    val prefix = ZipConstants.EXTRA_INFO + ZipConstants.PATH_SEPARATOR
    val entries = zipFile.entries()

    while (entries.hasMoreElements()) {
      val zipEntry: ZipEntry = entries.nextElement()
      val name: String = zipEntry.name

      if (name.startsWith(prefix)) {
        val inputStream: InputStream = zipFile.getInputStream(zipEntry)
        val key = name.substring(prefix.length)
        val value: Serializable = Util.fromInputStream(Serializable::class.java, inputStream)

        inputStream.close()
        extraInformation[key] = value
      }
    }

    importDumpExtraInformation.processExtraInformation(extraInformation)
  }
}
