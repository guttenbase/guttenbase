package io.github.guttenbase.export

import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.exceptions.ExportException
import org.apache.commons.io.input.ReaderInputStream
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.nio.charset.StandardCharsets
import java.sql.*
import java.sql.Date
import java.util.*

/**
 * Custom implementation of [PreparedStatement] dumping data to the given output stream. Only few inherited setter methods
 * have a meaningful implementation, most methods will throw a [UnsupportedOperationException].
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpPreparedStatement(private val exporter: Exporter) : PreparedStatement {
  override fun execute()=true

  override fun setObject(parameterIndex: Int, x: Any?) {
    try {
      exporter.writeObject(x)
    } catch (e: Exception) {
      throw ExportException("setObject", e)
    }
  }

  override fun setBoolean(parameterIndex: Int, x: Boolean) {
    setObject(parameterIndex, x)
  }

  override fun setByte(parameterIndex: Int, x: Byte) {
    setObject(parameterIndex, x)
  }

  override fun setShort(parameterIndex: Int, x: Short) {
    setObject(parameterIndex, x)
  }

  override fun setInt(parameterIndex: Int, x: Int) {
    setObject(parameterIndex, x)
  }

  override fun setLong(parameterIndex: Int, x: Long) {
    setObject(parameterIndex, x)
  }

  override fun setFloat(parameterIndex: Int, x: Float) {
    setObject(parameterIndex, x)
  }

  override fun setDouble(parameterIndex: Int, x: Double) {
    setObject(parameterIndex, x)
  }

  override fun setBigDecimal(parameterIndex: Int, x: BigDecimal) {
    setObject(parameterIndex, x)
  }

  override fun setString(parameterIndex: Int, x: String) {
    setObject(parameterIndex, x)
  }

  override fun setBytes(parameterIndex: Int, x: ByteArray) {
    setObject(parameterIndex, x)
  }

  override fun setDate(parameterIndex: Int, x: Date) {
    setObject(parameterIndex, x)
  }

  override fun setTime(parameterIndex: Int, x: Time) {
    setObject(parameterIndex, x)
  }

  override fun setTimestamp(parameterIndex: Int, x: Timestamp) {
    setObject(parameterIndex, x)
  }

  override fun setNull(parameterIndex: Int, sqlType: Int) {
    setObject(parameterIndex, null)
  }

  @Throws(SQLException::class)
  override fun setClob(parameterIndex: Int, clob: Clob?) {
    if (clob != null) {
      setObject(parameterIndex, ExportDumpClob(clob.getAsciiStream()))
      flush()
      clob.free()
    } else {
      setObject(parameterIndex, null)
    }
  }

  override fun setClob(parameterIndex: Int, reader: Reader) {
    val readerInputStream = ReaderInputStream.builder().setCharsetEncoder(StandardCharsets.UTF_8.newEncoder()).setReader(reader).get()
    setObject(parameterIndex, ExportDumpClob(readerInputStream))
    flush()
  }

  @Throws(SQLException::class)
  override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML?) {
    if (xmlObject != null) {
      setObject(parameterIndex, ExportDumpSqlXML(xmlObject.binaryStream))
      flush()
      xmlObject.free()
    } else {
      setObject(parameterIndex, null)
    }
  }

  override fun setBlob(parameterIndex: Int, inputStream: InputStream) {
    setObject(parameterIndex, ExportDumpBlob(inputStream))
    flush()
  }

  @Throws(SQLException::class)
  override fun setBlob(parameterIndex: Int, x: Blob?) {
    if (x != null) {
      setBlob(parameterIndex, x.binaryStream)
      x.free()
    } else {
      setObject(parameterIndex, null)
    }
  }

  override fun executeQuery(sql: String): ResultSet {
    throw UnsupportedOperationException()
  }

  override fun executeUpdate(sql: String): Int {
    throw UnsupportedOperationException()
  }

  override fun close() {
    // Ignored
  }

  override fun getMaxFieldSize(): Int {
    throw UnsupportedOperationException()
  }

  override fun setMaxFieldSize(max: Int) {
    throw UnsupportedOperationException()
  }

  override fun getMaxRows(): Int {
    throw UnsupportedOperationException()
  }

  override fun setMaxRows(max: Int) {
    throw UnsupportedOperationException()
  }

  override fun setEscapeProcessing(enable: Boolean) {
    throw UnsupportedOperationException()
  }

  override fun getQueryTimeout(): Int {
    throw UnsupportedOperationException()
  }

  override fun setQueryTimeout(seconds: Int) {
    throw UnsupportedOperationException()
  }

  override fun cancel() {
    throw UnsupportedOperationException()
  }

  override fun getWarnings(): SQLWarning {
    throw UnsupportedOperationException()
  }

  override fun clearWarnings() {
    throw UnsupportedOperationException()
  }

  override fun setCursorName(name: String) {
    throw UnsupportedOperationException()
  }

  override fun execute(sql: String): Boolean {
    return false
  }

  override fun executeUpdate(): Int {
    return 0
  }

  override fun getResultSet(): ResultSet {
    throw UnsupportedOperationException()
  }

  override fun getUpdateCount(): Int {
    throw UnsupportedOperationException()
  }

  override fun getMoreResults(): Boolean {
    return false
  }

  override fun setFetchDirection(direction: Int) {
    throw UnsupportedOperationException()
  }

  override fun getFetchDirection(): Int {
    throw UnsupportedOperationException()
  }

  override fun setFetchSize(rows: Int) {
    throw UnsupportedOperationException()
  }

  override fun getFetchSize(): Int {
    throw UnsupportedOperationException()
  }

  override fun getResultSetConcurrency(): Int {
    throw UnsupportedOperationException()
  }

  override fun getResultSetType(): Int {
    throw UnsupportedOperationException()
  }

  override fun addBatch(sql: String) {
    throw UnsupportedOperationException()
  }

  override fun clearBatch() {
    // Ignored
  }

  override fun executeBatch(): IntArray {
    return IntArray(0)
  }

  override fun getConnection(): Connection {
    throw UnsupportedOperationException()
  }

  override fun getMoreResults(current: Int): Boolean {
    return false
  }

  override fun getGeneratedKeys(): ResultSet {
    throw UnsupportedOperationException()
  }

  override fun executeUpdate(sql: String, autoGeneratedKeys: Int): Int {
    throw UnsupportedOperationException()
  }

  override fun executeUpdate(sql: String, columnIndexes: IntArray): Int {
    throw UnsupportedOperationException()
  }

  override fun executeUpdate(sql: String, columnNames: Array<String>): Int {
    throw UnsupportedOperationException()
  }

  override fun execute(sql: String, autoGeneratedKeys: Int): Boolean {
    return false
  }

  override fun execute(sql: String, columnIndexes: IntArray): Boolean {
    return false
  }

  override fun execute(sql: String, columnNames: Array<String>): Boolean {
    return false
  }

  override fun getResultSetHoldability(): Int {
    throw UnsupportedOperationException()
  }

  override fun isClosed(): Boolean {
    return false
  }

  override fun setPoolable(poolable: Boolean) {
    throw UnsupportedOperationException()
  }

  override fun isPoolable(): Boolean {
    return false
  }

  override fun <T> unwrap(iface: Class<T>): T {
    throw UnsupportedOperationException()
  }

  override fun isWrapperFor(iface: Class<*>?): Boolean {
    return false
  }

  override fun executeQuery(): ResultSet {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(parameterIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  @Deprecated("Deprecated in Java", ReplaceWith("throw UnsupportedOperationException()"))
  override fun setUnicodeStream(parameterIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun setBinaryStream(parameterIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun clearParameters() {
    throw UnsupportedOperationException()
  }

  override fun setObject(parameterIndex: Int, x: Any, targetSqlType: Int) {
    throw UnsupportedOperationException()
  }

  override fun addBatch() {
    // Ignored
  }

  override fun setCharacterStream(parameterIndex: Int, reader: Reader, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun setRef(parameterIndex: Int, x: Ref) {
    throw UnsupportedOperationException()
  }

  override fun setArray(parameterIndex: Int, x: java.sql.Array) {
    throw UnsupportedOperationException()
  }

  override fun getMetaData(): ResultSetMetaData {
    throw UnsupportedOperationException()
  }

  override fun setDate(parameterIndex: Int, x: Date, cal: Calendar) {
    throw UnsupportedOperationException()
  }

  override fun setTime(parameterIndex: Int, x: Time, cal: Calendar) {
    throw UnsupportedOperationException()
  }

  override fun setTimestamp(parameterIndex: Int, x: Timestamp, cal: Calendar) {
    throw UnsupportedOperationException()
  }

  override fun setNull(parameterIndex: Int, sqlType: Int, typeName: String) {
    setNull(parameterIndex, Types.JAVA_OBJECT)
  }

  override fun setURL(parameterIndex: Int, x: URL) {
    throw UnsupportedOperationException()
  }

  override fun getParameterMetaData(): ParameterMetaData {
    throw UnsupportedOperationException()
  }

  override fun setRowId(parameterIndex: Int, x: RowId) {
    throw UnsupportedOperationException()
  }

  override fun setNString(parameterIndex: Int, value: String) {
    throw UnsupportedOperationException()
  }

  override fun setNCharacterStream(parameterIndex: Int, value: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setNClob(parameterIndex: Int, value: NClob) {
    throw UnsupportedOperationException()
  }

  override fun setClob(parameterIndex: Int, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setBlob(parameterIndex: Int, inputStream: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setNClob(parameterIndex: Int, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setObject(parameterIndex: Int, x: Any, targetSqlType: Int, scaleOrLength: Int) {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(parameterIndex: Int, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setBinaryStream(parameterIndex: Int, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(parameterIndex: Int, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(parameterIndex: Int, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun setBinaryStream(parameterIndex: Int, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(parameterIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun setNCharacterStream(parameterIndex: Int, value: Reader) {
    throw UnsupportedOperationException()
  }

  override fun setNClob(parameterIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  private fun flush() {
    try {
      exporter.flush()
    } catch (e: Exception) {
      throw GuttenBaseException("flush", e)
    }
  }

  // JRE 1.7
  override fun closeOnCompletion() {
    throw UnsupportedOperationException()
  }

  override fun isCloseOnCompletion(): Boolean {
    throw UnsupportedOperationException()
  }
}
