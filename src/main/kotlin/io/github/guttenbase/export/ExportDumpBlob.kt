package io.github.guttenbase.export

import java.io.InputStream
import java.io.OutputStream
import java.sql.Blob

/**
 * Since BLOBs may be quite big. we do not load them into memory completely, but
 * read them in chunks and write the data to the output stream in a loop.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpBlob(inputStream: InputStream) : AbstractExportDumpObject(inputStream), Blob {
  override fun position(pattern: ByteArray, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun position(pattern: Blob, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun setBytes(pos: Long, bytes: ByteArray): Int {
    throw UnsupportedOperationException()
  }

  override fun setBytes(pos: Long, bytes: ByteArray, offset: Int, len: Int): Int {
    throw UnsupportedOperationException()
  }

  override fun setBinaryStream(pos: Long): OutputStream {
    throw UnsupportedOperationException()
  }

  override fun truncate(len: Long) {
    throw UnsupportedOperationException()
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
