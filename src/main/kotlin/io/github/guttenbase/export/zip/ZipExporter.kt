package io.github.guttenbase.export.zip


import io.github.guttenbase.export.ExportDumpConnectorInfo
import io.github.guttenbase.export.ExportDumpExtraInformation
import io.github.guttenbase.export.ExportTableHeader
import io.github.guttenbase.export.Exporter
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.*
import java.sql.SQLException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Export schema information and data into executable JAR file. Since it is in ZIP file format the resulting file may as well be
 * inspected with a ZIP tool. The structure of the ZIP is based on the structure of a data base.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [ZipExporterClassResourcesHint] to add custom classes to the generated JAR and configure the
 * META-INF/MANIFEST.MF Main-Class entry
 * Hint is used by [io.github.guttenbase.export.ExportDumpExtraInformationHint] to add custom information to the generated JAR
 */
class ZipExporter : Exporter {
  private lateinit var zipOutputStream: ZipOutputStream
  private lateinit var objectOutputStream: ObjectOutputStream
  private lateinit var tempFile: File
  private lateinit var connectorRepository: ConnectorRepository
  private lateinit var connectorId: String
  private lateinit var exportDumpConnectionInfo: ExportDumpConnectorInfo

  /**
   * {@inheritDoc}
   */
  @Throws(IOException::class)
  override fun initializeExport(
    connectorRepository: ConnectorRepository,
    connectorId: String,
    exportDumpConnectionInfo: ExportDumpConnectorInfo
  ) {
    this.connectorRepository = connectorRepository
    this.connectorId = connectorId
    this.exportDumpConnectionInfo = exportDumpConnectionInfo

    val file = File(exportDumpConnectionInfo.path)
    val fos = FileOutputStream(file)
    val zipExporterClassResources = connectorRepository.getConnectorHint(connectorId, ZipExporterClassResources::class.java).value

    zipOutputStream = ZipOutputStream(fos)

    addClassesToJar(zipExporterClassResources)
    writeManifestEntry(zipExporterClassResources)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(Exception::class)
  override fun finishExport() {
    writeExtraInformation()

    val zipExporterClassResources = connectorRepository.getConnectorHint(connectorId, ZipExporterClassResources::class.java).value

    addResourcesToJar(zipExporterClassResources)
    zipOutputStream.close()
  }

  /**
   * {@inheritDoc}
   */
  @Throws(IOException::class, SQLException::class)
  override fun writeDatabaseMetaData(databaseMetaData: DatabaseMetaData) {
    writeDatabaseEntry(databaseMetaData)

    for (tableMetaData in databaseMetaData.tableMetaData) {
      writeTableEntry(tableMetaData)
      writeColumnEntries(tableMetaData)
      writeIndexEntries(tableMetaData)
    }
  }

  /**
   * {@inheritDoc}
   *
   *
   * Does nothing.
   */
  @Throws(IOException::class, SQLException::class)
  override fun writeTableHeader(exportTableHeader: ExportTableHeader) {
  }

  /**
   * {@inheritDoc}
   *
   *
   * Open new ZIP entry. Data will be written to a temporary file first, because otherwise it may exceed the memory.
   */
  @Throws(IOException::class)
  override fun initializeWriteTableData(tableMetaData: TableMetaData) {
    newEntry((ZipConstants.PREFIX + tableMetaData.tableName + ZipConstants.PATH_SEPARATOR) + ZipConstants.TABLE_DATA_NAME)

    tempFile = File.createTempFile("GB-JAR-", null)
    tempFile.deleteOnExit()
    objectOutputStream = ObjectOutputStream(FileOutputStream(tempFile))
  }

  /**
   * {@inheritDoc}
   *
   *
   * Close current ZIP entry.
   */
  @Throws(IOException::class)
  override fun finalizeWriteTableData(tableMetaData: TableMetaData) {
    objectOutputStream.close()

    val fis = FileInputStream(tempFile)
    IOUtils.copy(fis, zipOutputStream, Util.DEFAULT_BUFFER_SIZE)
    IOUtils.closeQuietly(fis)

    closeEntry()
    tempFile.delete()
  }

  /**
   * {@inheritDoc}
   *
   *
   * Does nothing.
   */
  override fun initializeWriteRowData(tableMetaData: TableMetaData) {}

  /**
   * {@inheritDoc}
   *
   *
   * Does nothing.
   */
  override fun finalizeWriteRowData(tableMetaData: TableMetaData) {}

  /**
   * Resets the output stream which reduces memory foot print drastically. See [ObjectOutputStream.reset] for details.
   */
  @Throws(IOException::class)
  override fun flush() {
    if (this::objectOutputStream.isInitialized) {
      objectOutputStream.reset()
      objectOutputStream.flush()
    }
  }

  @Throws(IOException::class)
  override fun writeObject(obj: Any?) {
    objectOutputStream.writeObject(obj)
  }

  @Throws(IOException::class)
  private fun writeIndexEntries(tableMetaData: TableMetaData) {
    val indexPath: String = (ZipConstants.PREFIX + tableMetaData.tableName + ZipConstants.PATH_SEPARATOR
        ) + ZipConstants.INDEX_NAME + ZipConstants.PATH_SEPARATOR

    for (indexMetaData in tableMetaData.indexes) {
      newEntry(indexPath + indexMetaData.indexName + ".txt")
      ZipIndexMetaDataWriter().writeIndexMetaDataEntry(indexMetaData).store("Index meta data", zipOutputStream)
      closeEntry()
    }
  }

  @Throws(IOException::class)
  private fun writeColumnEntries(tableMetaData: TableMetaData) {
    val columnPath: String =
      (ZipConstants.PREFIX + tableMetaData.tableName + ZipConstants.PATH_SEPARATOR) + ZipConstants.COLUMN_NAME + ZipConstants.PATH_SEPARATOR
    for (columnMetaData in tableMetaData.columnMetaData) {
      newEntry(columnPath + columnMetaData.columnName + ".txt")
      ZipColumnMetaDataWriter().writeColumnMetaDataEntry(columnMetaData).store("Column meta data", zipOutputStream)
      closeEntry()
    }
  }

  @Throws(IOException::class)
  private fun writeTableEntry(tableMetaData: TableMetaData) {
    newEntry((ZipConstants.PREFIX + tableMetaData.tableName + ZipConstants.PATH_SEPARATOR) + ZipConstants.TABLE_INFO_NAME)
    ZipTableMetaDataWriter().writeTableMetaDataEntry(tableMetaData).store("Table meta data", zipOutputStream)
    closeEntry()
  }

  @Throws(IOException::class, SQLException::class)
  private fun writeDatabaseEntry(databaseMetaData: DatabaseMetaData) {
    newEntry(ZipConstants.PREFIX + ZipConstants.DBINFO_NAME)
    ZipDatabaseMetaDataWriter().writeDatabaseMetaDataEntry(databaseMetaData).store("Database meta data", zipOutputStream)
    closeEntry()
    newEntry(ZipConstants.META_DATA)
    zipOutputStream.write(Util.toByteArray(databaseMetaData))
    closeEntry()
  }

  @Throws(IOException::class)
  private fun newEntry(name: String) {
    zipOutputStream.putNextEntry(ZipEntry(name))
  }

  @Throws(IOException::class)
  private fun closeEntry() {
    zipOutputStream.closeEntry()
  }

  @Throws(IOException::class)
  private fun addClassesToJar(zipExporterClassResources: ZipExporterClassResources) {
    val zipClassesFromClassResourceExporter = ZipClassesFromClassResourceExporter(zipOutputStream)

    for (clazz in zipExporterClassResources.classResources) {
      zipClassesFromClassResourceExporter.copyClassesToZip(clazz)
    }
  }

  @Throws(IOException::class)
  private fun addResourcesToJar(zipExporterClassResources: ZipExporterClassResources) {
    val zipResourceExporter = ZipResourceExporter(zipOutputStream)

    for ((key, url) in zipExporterClassResources.urlResources) {
      if (url != null) {
        val stream = url.openStream()
        zipResourceExporter.addEntry(key, stream)
      } else {
        LOG.warn("Could not add null URL content for $key")
      }
    }
  }

  @Throws(IOException::class)
  private fun writeManifestEntry(zipExporterClassResources: ZipExporterClassResources) {
    newEntry(ZipConstants.MANIFEST_NAME)
    val bos = ByteArrayOutputStream()
    val printStream = PrintStream(bos)

    printStream.println("Manifest-Version: 1.0")
    printStream.println("Created-By: GuttenBase ZIP Exporter")
    printStream.println("Main-Class: " + zipExporterClassResources.startupClass.name)
    printStream.close()

    zipOutputStream.write(bos.toByteArray())
    closeEntry()
  }

  @Throws(IOException::class, SQLException::class)
  private fun writeExtraInformation() {
    val exportDumpExtraInformation: ExportDumpExtraInformation = connectorRepository.getConnectorHint(
      connectorId,
      ExportDumpExtraInformation::class.java
    ).value
    val extraInformation: Map<String, Serializable> = exportDumpExtraInformation.getExtraInformation(
      connectorRepository,
      connectorId, exportDumpConnectionInfo
    )

    for ((key, value) in extraInformation) {
      newEntry(ZipConstants.EXTRA_INFO + ZipConstants.PATH_SEPARATOR + key)

      val byteArray: ByteArray = Util.toByteArray(value)
      zipOutputStream.write(byteArray)
      closeEntry()
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(ZipExporter::class.java)
  }
}
