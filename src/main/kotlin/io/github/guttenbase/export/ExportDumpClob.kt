package io.github.guttenbase.export

import java.io.*
import java.sql.Clob

/**
 * Since CLOBs may be quite big. we do not load them into memory completely, but read them in chunks and write the data to the output stream
 * in a loop.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class ExportDumpClob(inputStream: InputStream) : AbstractExportDumpObject(inputStream), Clob {
  // Serialization
  protected constructor() : this(ByteArrayInputStream(ByteArray(0)))

  override fun getAsciiStream() = getBinaryStream()

  override fun getCharacterStream() = InputStreamReader(getBinaryStream())

  override fun getCharacterStream(pos: Long, length: Long) = InputStreamReader(getBinaryStream(pos, length))

  override fun getSubString(pos: Long, length: Int): String {
    throw UnsupportedOperationException()
  }

  override fun position(searchstr: String, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun position(searchstr: Clob, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(pos: Long): OutputStream {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(pos: Long): Writer {
    throw UnsupportedOperationException()
  }

  override fun setString(pos: Long, str: String): Int {
    throw UnsupportedOperationException()
  }

  override fun setString(pos: Long, str: String, offset: Int, len: Int): Int {
    throw UnsupportedOperationException()
  }

  override fun truncate(len: Long) {
    throw UnsupportedOperationException()
  }

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}
