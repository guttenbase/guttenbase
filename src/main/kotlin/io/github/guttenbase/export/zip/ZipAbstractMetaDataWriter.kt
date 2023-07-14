package io.github.guttenbase.export.zip

import java.io.*
import java.util.*
import java.util.zip.ZipOutputStream

/**
 * Base implementation the gathers all properties and stores them into a file in the ZIP.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class ZipAbstractMetaDataWriter {
  val properties: Properties = object : Properties() {
    override fun keys(): Enumeration<Any> = Collections.enumeration(super.keys().toList().map { it.toString() }.sorted())
  }

  protected fun setProperty(key: String, value: String) {
    properties.setProperty(key, value)
  }

  /**
   * Store gathered properties as text in current ZIP file entry.
   */
  @Throws(IOException::class)
  fun store(comment: String, zipOutputStream: ZipOutputStream) {
    val bos = ByteArrayOutputStream()
    val printStream = PrintStream(bos)
    properties.store(printStream, comment)
    printStream.close()
    zipOutputStream.write(bos.toByteArray())
  }
}
