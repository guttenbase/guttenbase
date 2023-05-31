package io.github.guttenbase.repository.export

import de.akquinet.jbosscc.guttenbase.exceptions.MissingDataException
import io.github.guttenbase.meta.TableMetaData
import java.io.InputStream
import java.io.Reader
import java.net.URL
import java.sql.*

/**
 * Custom implementation of [PreparedStatement] reading data from the given input stream. This done via the custom
 * [ImportDumpResultSet] object return by [.executeQuery].
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ImportDumpPreparedStatement(
  importer: Importer?,
  databaseMetaData: DatabaseMetaData?,
  tableMetaData: TableMetaData?,
  selectSql: String
) : PreparedStatement {
  private val _importer: Importer?
  private val _tableMetaData: TableMetaData?
  private val _selectSql: String
  private val _databaseMetaData: DatabaseMetaData?

  init {
    assert(importer != null) { "importer != null" }
    assert(databaseMetaData != null) { "databaseMetaData != null" }
    assert(tableMetaData != null) { "tableMetaData != null" }
    assert(selectSql != null) { "selectSql != null" }
    _importer = importer
    _tableMetaData = tableMetaData
    _selectSql = selectSql
    _databaseMetaData = databaseMetaData
  }

  override fun executeQuery(): ResultSet {
    return executeQuery(_selectSql)
  }

  override fun executeQuery(sql: String): ResultSet {
    assert(sql != null) { "sql != null" }
    if (_tableMetaData.getFilteredRowCount() < 0) {
      throw MissingDataException("Invalid number of expected rows")
    }
    return ImportDumpResultSet(_importer, _databaseMetaData, _tableMetaData, Util.parseSelectedColumns(sql))
  }

  override fun execute(): Boolean {
    return true
  }

  override fun setFetchSize(expectedRows: Int) {}
  override fun close() {}
  override fun getFetchSize(): Int {
    throw UnsupportedOperationException()
  }

  override fun setBoolean(parameterIndex: Int, x: Boolean) {
    throw UnsupportedOperationException()
  }

  override fun setByte(parameterIndex: Int, x: Byte) {
    throw UnsupportedOperationException()
  }

  override fun setShort(parameterIndex: Int, x: Short) {
    throw UnsupportedOperationException()
  }

  override fun setInt(parameterIndex: Int, x: Int) {
    throw UnsupportedOperationException()
  }

  override fun setLong(parameterIndex: Int, x: Long) {
    throw UnsupportedOperationException()
  }

  override fun setFloat(parameterIndex: Int, x: Float) {
    throw UnsupportedOperationException()
  }

  override fun setDouble(parameterIndex: Int, x: Double) {
    throw UnsupportedOperationException()
  }

  override fun setBigDecimal(parameterIndex: Int, x: BigDecimal) {
    throw UnsupportedOperationException()
  }

  override fun setString(parameterIndex: Int, x: String) {
    throw UnsupportedOperationException()
  }

  override fun setBytes(parameterIndex: Int, x: ByteArray) {
    throw UnsupportedOperationException()
  }

  override fun setDate(parameterIndex: Int, x: Date) {
    throw UnsupportedOperationException()
  }

  override fun setTime(parameterIndex: Int, x: Time) {
    throw UnsupportedOperationException()
  }

  override fun setTimestamp(parameterIndex: Int, x: Timestamp) {
    throw UnsupportedOperationException()
  }

  override fun setBlob(parameterIndex: Int, x: Blob) {
    throw UnsupportedOperationException()
  }

  override fun executeUpdate(sql: String): Int {
    throw UnsupportedOperationException()
  }

  override fun getMaxFieldSize(): Int {
    throw UnsupportedOperationException()
  }

  override fun setMaxFieldSize(max: Int) {
    throw UnsupportedOperationException()
  }

  override fun getMaxRows(): Int {
    return 2000
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
    return null
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

  override fun getResultSet(): ResultSet {
    return null
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
    throw UnsupportedOperationException()
  }

  override fun executeBatch(): IntArray {
    return null
  }

  override fun getConnection(): Connection {
    return null
  }

  override fun getMoreResults(current: Int): Boolean {
    return false
  }

  override fun getGeneratedKeys(): ResultSet {
    return null
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
    return null
  }

  override fun isWrapperFor(iface: Class<*>?): Boolean {
    return false
  }

  override fun executeUpdate(): Int {
    throw UnsupportedOperationException()
  }

  override fun setNull(parameterIndex: Int, sqlType: Int) {
    throw UnsupportedOperationException()
  }

  override fun setAsciiStream(parameterIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

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

  override fun setObject(parameterIndex: Int, x: Any) {
    throw UnsupportedOperationException()
  }

  override fun addBatch() {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(parameterIndex: Int, reader: Reader, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun setRef(parameterIndex: Int, x: Ref) {
    throw UnsupportedOperationException()
  }

  override fun setClob(parameterIndex: Int, x: Clob) {
    throw UnsupportedOperationException()
  }

  override fun setArray(parameterIndex: Int, x: java.sql.Array) {
    throw UnsupportedOperationException()
  }

  override fun getMetaData(): ResultSetMetaData {
    return null
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
    throw UnsupportedOperationException()
  }

  override fun setURL(parameterIndex: Int, x: URL) {
    throw UnsupportedOperationException()
  }

  override fun getParameterMetaData(): ParameterMetaData {
    return null
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

  override fun setSQLXML(parameterIndex: Int, xmlObject: SQLXML) {
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

  override fun setClob(parameterIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun setBlob(parameterIndex: Int, inputStream: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun setNClob(parameterIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  // JRE 1.7
  override fun closeOnCompletion() {
    throw UnsupportedOperationException()
  }

  override fun isCloseOnCompletion(): Boolean {
    throw UnsupportedOperationException()
  }
}
