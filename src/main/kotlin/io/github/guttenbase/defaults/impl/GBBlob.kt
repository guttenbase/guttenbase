package io.github.guttenbase.defaults.impl

import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.OutputStream
import java.sql.Blob

/**
 * Since BLOBs may be quite big. we do not load them into memory completely, but
 * read them in chunks and write the data to the output stream in a loop.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class GBBlob(private val inputStream: InputStream) : Blob {
  constructor(bytes: ByteArray) : this(ByteArrayInputStream(bytes))

  override fun length() = inputStream.available().toLong()

  override fun getBytes(pos: Long, length: Int): ByteArray = inputStream.readNBytes(length)

  override fun getBinaryStream(): InputStream = inputStream

  override fun getBinaryStream(pos: Long, length: Long): InputStream = getBinaryStream()

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

  override fun free() {
    inputStream.close()
  }
}
