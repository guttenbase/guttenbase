package io.github.guttenbase.export.zip


import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/**
 * Helper class to add resources to ZIP file.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class ZipResourceExporter(private val zipOutputStream: ZipOutputStream) {
  private val entries = HashSet<String>()

  fun addEntry(n: String, inputStream: InputStream) {
    // Escape problems with DOS/Windows in ZIP entries
    val name = n.replace('\\', ZipConstants.PATH_SEPARATOR)

    if (!entries.add(name.uppercase()) || name.equals(
        ZipConstants.MANIFEST_NAME,
        ignoreCase = true
      )
    ) {
      LOG.warn("Duplicate entry ignored: $name")
    } else {
      val zipEntry = ZipEntry(name)

      zipOutputStream.putNextEntry(zipEntry)
      IOUtils.copy(inputStream, zipOutputStream, DEFAULT_BUFFER_SIZE)
      inputStream.close()

      zipOutputStream.closeEntry()
    }
  }

  companion object {
    @JvmStatic
    protected val LOG: Logger = LoggerFactory.getLogger(ZipResourceExporter::class.java)
  }
}
