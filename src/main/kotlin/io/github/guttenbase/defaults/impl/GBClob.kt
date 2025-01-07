package io.github.guttenbase.defaults.impl

import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.sql.Clob

/**
 * Since BLOBs may be quite big. we do not load them into memory completely, but
 * read them in chunks and write the data to the output stream in a loop.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class GBClob(private val inputStream: InputStream) : Clob {
  constructor(value: String) : this(value.byteInputStream())

  override fun length() = inputStream.available().toLong()

  override fun getSubString(pos: Long, length: Int): String? {
    throw UnsupportedOperationException()
  }

  override fun getCharacterStream(): Reader = InputStreamReader(inputStream)

  override fun getAsciiStream(): InputStream = inputStream

  override fun position(searchstr: String?, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun position(searchstr: Clob?, start: Long): Long {
    throw UnsupportedOperationException()
  }

  override fun setString(pos: Long, str: String?): Int {
    throw UnsupportedOperationException()
  }

  override fun setString(pos: Long, str: String?, offset: Int, len: Int): Int {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(pos: Long): OutputStream? {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(pos: Long): Writer? {
    throw UnsupportedOperationException()
  }

  override fun truncate(len: Long) {
    throw UnsupportedOperationException()
  }

  override fun free() {
    inputStream.close()
  }

  override fun getCharacterStream(pos: Long, length: Long): Reader? = characterStream
}
