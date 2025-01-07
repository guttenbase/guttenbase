package io.github.guttenbase.export

import io.github.guttenbase.defaults.impl.DefaultColumnComparator
import io.github.guttenbase.exceptions.ImportException
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import java.io.InputStream
import java.io.Reader
import java.math.BigDecimal
import java.net.URL
import java.sql.*
import java.sql.Array
import java.sql.Date
import java.util.*

/**
 * Special [ResultSet] that reads data from the given stream. Only few inherited getter methods have a meaningful
 * implementation, most methods will throw a [UnsupportedOperationException].
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ImportDumpResultSet(
  private val importer: Importer,
  databaseMetaData: DatabaseMetaData,
  private val tableMetaData: TableMetaData,
  selectedColumns: List<String>
) : ResultSet {
  private var rowCount = 0

  private var wasNull = false


  /**
   * Since tableMetaData may contain a limited set of columns, but the dumped data contains all columns, we need to map the
   * indices.
   */
  private val columnIndexMap = HashMap<Int, Int>()
  private val currentRow = ArrayList<Any?>()
  private val origTableMetaData = databaseMetaData.getTableMetaData(tableMetaData.tableName)!!

  init {
    buildColumnIndexMap(selectedColumns)
  }

  private fun buildColumnIndexMap(selectedColumns: List<String>) {
    val columnMetaData = origTableMetaData.columnMetaData.sortedWith(DefaultColumnComparator())

    for (originalColumnIndex in columnMetaData.indices) {
      val column = columnMetaData[originalColumnIndex].columnName.uppercase()
      val columnIndex = selectedColumns.indexOf(column)

      if (columnIndex >= 0) {
        columnIndexMap[columnIndex + 1] = originalColumnIndex + 1
      }
    }
  }

  override fun next(): Boolean {
    currentRow.clear()

    val hasNext: Boolean = rowCount++ < tableMetaData.totalRowCount

    if (hasNext) // Prefetch current row
    {
      (0 until origTableMetaData.columnCount).forEach { currentRow.add(readObject()) }
    }

    return hasNext
  }

  override fun getObject(columnIndex: Int): Any? {
    val realIndex = columnIndexMap[columnIndex]!!
    val result = currentRow[realIndex - 1]

    wasNull = result == null

    return result
  }

  private fun readObject(): Any? {
    return try {
      importer.readObject()
    } catch (e: Exception) {
      throw ImportException("readObject", e)
    }
  }

  override fun getBoolean(columnIndex: Int): Boolean {
    val `object` = getObject(columnIndex) as Boolean?
    return `object` != null && `object`
  }

  override fun getByte(columnIndex: Int): Byte {
    val `object` = getObject(columnIndex) as Byte?
    return `object` ?: 0
  }

  override fun getShort(columnIndex: Int): Short {
    val `object` = getObject(columnIndex) as Short?
    return `object` ?: 0
  }

  override fun getInt(columnIndex: Int): Int {
    val `object` = getObject(columnIndex) as Int?
    return `object` ?: 0
  }

  override fun getLong(columnIndex: Int): Long {
    val `object` = getObject(columnIndex) as Long?
    return `object` ?: 0
  }

  override fun getFloat(columnIndex: Int): Float {
    val `object` = getObject(columnIndex) as Float?
    return `object` ?: 0.0F
  }

  override fun getDouble(columnIndex: Int): Double {
    val `object` = getObject(columnIndex) as Double?
    return `object` ?: 0.0
  }

  override fun getBlob(columnIndex: Int): Blob? {
    return getObject(columnIndex) as Blob?
  }

  override fun getClob(columnIndex: Int): Clob? {
    return getObject(columnIndex) as Clob?
  }

  override fun getSQLXML(columnIndex: Int): SQLXML? {
    return getObject(columnIndex) as SQLXML?
  }

  override fun getString(columnIndex: Int): String? {
    return getObject(columnIndex) as String?
  }

  @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
  override fun getBigDecimal(columnIndex: Int, scale: Int): BigDecimal? {
    return getBigDecimal(columnIndex)
  }

  override fun getBigDecimal(columnIndex: Int): BigDecimal? {
    return getObject(columnIndex) as BigDecimal?
  }

  override fun getBytes(columnIndex: Int): ByteArray? {
    return getObject(columnIndex) as ByteArray?
  }

  override fun getDate(columnIndex: Int): Date? {
    return getObject(columnIndex) as Date?
  }

  override fun getTime(columnIndex: Int): Time? {
    return getObject(columnIndex) as Time?
  }

  override fun getTimestamp(columnIndex: Int): Timestamp? {
    return getObject(columnIndex) as Timestamp?
  }

  override fun wasNull(): Boolean {
    return wasNull
  }

  override fun close() {
    currentRow.clear()
  }

  override fun <T> unwrap(iface: Class<T>): T {
    throw UnsupportedOperationException()
  }

  override fun isWrapperFor(iface: Class<*>?): Boolean {
    throw UnsupportedOperationException()
  }

  override fun getAsciiStream(columnIndex: Int): InputStream {
    throw UnsupportedOperationException()
  }

  @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
  override fun getUnicodeStream(columnIndex: Int): InputStream {
    throw UnsupportedOperationException()
  }

  override fun getBinaryStream(columnIndex: Int): InputStream {
    throw UnsupportedOperationException()
  }

  override fun getString(columnLabel: String): String {
    throw UnsupportedOperationException()
  }

  override fun getBoolean(columnLabel: String): Boolean {
    throw UnsupportedOperationException()
  }

  override fun getByte(columnLabel: String): Byte {
    throw UnsupportedOperationException()
  }

  override fun getShort(columnLabel: String): Short {
    throw UnsupportedOperationException()
  }

  override fun getInt(columnLabel: String): Int {
    throw UnsupportedOperationException()
  }

  override fun getLong(columnLabel: String): Long {
    throw UnsupportedOperationException()
  }

  override fun getFloat(columnLabel: String): Float {
    throw UnsupportedOperationException()
  }

  override fun getDouble(columnLabel: String): Double {
    throw UnsupportedOperationException()
  }

  @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
  override fun getBigDecimal(columnLabel: String, scale: Int): BigDecimal {
    throw UnsupportedOperationException()
  }

  override fun getBytes(columnLabel: String): ByteArray {
    throw UnsupportedOperationException()
  }

  override fun getDate(columnLabel: String): Date {
    throw UnsupportedOperationException()
  }

  override fun getTime(columnLabel: String): Time {
    throw UnsupportedOperationException()
  }

  override fun getTimestamp(columnLabel: String): Timestamp {
    throw UnsupportedOperationException()
  }

  override fun getAsciiStream(columnLabel: String): InputStream {
    throw UnsupportedOperationException()
  }

  @Deprecated("Deprecated in Java", replaceWith = ReplaceWith(""))
  override fun getUnicodeStream(columnLabel: String): InputStream {
    throw UnsupportedOperationException()
  }

  override fun getBinaryStream(columnLabel: String): InputStream {
    throw UnsupportedOperationException()
  }

  override fun getWarnings(): SQLWarning {
    throw UnsupportedOperationException()
  }

  override fun clearWarnings() {
    // Ignored
  }

  override fun getCursorName(): String {
    throw UnsupportedOperationException()
  }

  override fun getMetaData(): ResultSetMetaData {
    throw UnsupportedOperationException()
  }

  override fun getObject(columnLabel: String): Any {
    throw UnsupportedOperationException()
  }

  override fun findColumn(columnLabel: String): Int {
    throw UnsupportedOperationException()
  }

  override fun getCharacterStream(columnIndex: Int): Reader {
    throw UnsupportedOperationException()
  }

  override fun getCharacterStream(columnLabel: String): Reader {
    throw UnsupportedOperationException()
  }

  override fun getBigDecimal(columnLabel: String): BigDecimal {
    throw UnsupportedOperationException()
  }

  override fun isBeforeFirst(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun isAfterLast(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun isFirst(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun isLast(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun beforeFirst() {
    throw UnsupportedOperationException()
  }

  override fun afterLast() {
    throw UnsupportedOperationException()
  }

  override fun first(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun last(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun getRow(): Int {
    return rowCount + 1
  }

  override fun absolute(row: Int): Boolean {
    throw UnsupportedOperationException()
  }

  override fun relative(rows: Int): Boolean {
    throw UnsupportedOperationException()
  }

  override fun previous(): Boolean {
    throw UnsupportedOperationException()
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

  override fun getType(): Int {
    throw UnsupportedOperationException()
  }

  override fun getConcurrency(): Int {
    throw UnsupportedOperationException()
  }

  override fun rowUpdated(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun rowInserted(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun rowDeleted(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun updateNull(columnIndex: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateBoolean(columnIndex: Int, x: Boolean) {
    throw UnsupportedOperationException()
  }

  override fun updateByte(columnIndex: Int, x: Byte) {
    throw UnsupportedOperationException()
  }

  override fun updateShort(columnIndex: Int, x: Short) {
    throw UnsupportedOperationException()
  }

  override fun updateInt(columnIndex: Int, x: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateLong(columnIndex: Int, x: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateFloat(columnIndex: Int, x: Float) {
    throw UnsupportedOperationException()
  }

  override fun updateDouble(columnIndex: Int, x: Double) {
    throw UnsupportedOperationException()
  }

  override fun updateBigDecimal(columnIndex: Int, x: BigDecimal) {
    throw UnsupportedOperationException()
  }

  override fun updateString(columnIndex: Int, x: String) {
    throw UnsupportedOperationException()
  }

  override fun updateBytes(columnIndex: Int, x: ByteArray) {
    throw UnsupportedOperationException()
  }

  override fun updateDate(columnIndex: Int, x: Date) {
    throw UnsupportedOperationException()
  }

  override fun updateTime(columnIndex: Int, x: Time) {
    throw UnsupportedOperationException()
  }

  override fun updateTimestamp(columnIndex: Int, x: Timestamp) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnIndex: Int, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnIndex: Int, x: Reader, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateObject(columnIndex: Int, x: Any, scaleOrLength: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateObject(columnIndex: Int, x: Any) {
    throw UnsupportedOperationException()
  }

  override fun updateNull(columnLabel: String) {
    throw UnsupportedOperationException()
  }

  override fun updateBoolean(columnLabel: String, x: Boolean) {
    throw UnsupportedOperationException()
  }

  override fun updateByte(columnLabel: String, x: Byte) {
    throw UnsupportedOperationException()
  }

  override fun updateShort(columnLabel: String, x: Short) {
    throw UnsupportedOperationException()
  }

  override fun updateInt(columnLabel: String, x: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateLong(columnLabel: String, x: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateFloat(columnLabel: String, x: Float) {
    throw UnsupportedOperationException()
  }

  override fun updateDouble(columnLabel: String, x: Double) {
    throw UnsupportedOperationException()
  }

  override fun updateBigDecimal(columnLabel: String, x: BigDecimal) {
    throw UnsupportedOperationException()
  }

  override fun updateString(columnLabel: String, x: String) {
    throw UnsupportedOperationException()
  }

  override fun updateBytes(columnLabel: String, x: ByteArray) {
    throw UnsupportedOperationException()
  }

  override fun updateDate(columnLabel: String, x: Date) {
    throw UnsupportedOperationException()
  }

  override fun updateTime(columnLabel: String, x: Time) {
    throw UnsupportedOperationException()
  }

  override fun updateTimestamp(columnLabel: String, x: Timestamp) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnLabel: String, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnLabel: String, x: InputStream, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnLabel: String, reader: Reader, length: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateObject(columnLabel: String, x: Any, scaleOrLength: Int) {
    throw UnsupportedOperationException()
  }

  override fun updateObject(columnLabel: String, x: Any) {
    throw UnsupportedOperationException()
  }

  override fun insertRow() {
    throw UnsupportedOperationException()
  }

  override fun updateRow() {
    throw UnsupportedOperationException()
  }

  override fun deleteRow() {
    throw UnsupportedOperationException()
  }

  override fun refreshRow() {
    throw UnsupportedOperationException()
  }

  override fun cancelRowUpdates() {
    throw UnsupportedOperationException()
  }

  override fun moveToInsertRow() {
    throw UnsupportedOperationException()
  }

  override fun moveToCurrentRow() {
    throw UnsupportedOperationException()
  }

  override fun getStatement(): Statement {
    throw UnsupportedOperationException()
  }

  override fun getObject(columnIndex: Int, map: Map<String?, Class<*>?>?): Any {
    throw UnsupportedOperationException()
  }

  override fun getRef(columnIndex: Int): Ref {
    throw UnsupportedOperationException()
  }

  override fun getArray(columnIndex: Int): Array {
    throw UnsupportedOperationException()
  }

  override fun getObject(columnLabel: String, map: Map<String?, Class<*>?>?): Any {
    throw UnsupportedOperationException()
  }

  override fun getRef(columnLabel: String): Ref {
    throw UnsupportedOperationException()
  }

  override fun getBlob(columnLabel: String): Blob {
    throw UnsupportedOperationException()
  }

  override fun getClob(columnLabel: String): Clob {
    throw UnsupportedOperationException()
  }

  override fun getArray(columnLabel: String): Array {
    throw UnsupportedOperationException()
  }

  override fun getDate(columnIndex: Int, cal: Calendar): Date {
    throw UnsupportedOperationException()
  }

  override fun getDate(columnLabel: String, cal: Calendar): Date {
    throw UnsupportedOperationException()
  }

  override fun getTime(columnIndex: Int, cal: Calendar): Time {
    throw UnsupportedOperationException()
  }

  override fun getTime(columnLabel: String, cal: Calendar): Time {
    throw UnsupportedOperationException()
  }

  override fun getTimestamp(columnIndex: Int, cal: Calendar): Timestamp {
    throw UnsupportedOperationException()
  }

  override fun getTimestamp(columnLabel: String, cal: Calendar): Timestamp {
    throw UnsupportedOperationException()
  }

  override fun getURL(columnIndex: Int): URL {
    throw UnsupportedOperationException()
  }

  override fun getURL(columnLabel: String): URL {
    throw UnsupportedOperationException()
  }

  override fun updateRef(columnIndex: Int, x: Ref) {
    throw UnsupportedOperationException()
  }

  override fun updateRef(columnLabel: String, x: Ref) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnIndex: Int, x: Blob) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnLabel: String, x: Blob) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnIndex: Int, x: Clob) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnLabel: String, x: Clob) {
    throw UnsupportedOperationException()
  }

  override fun updateArray(columnIndex: Int, x: Array) {
    throw UnsupportedOperationException()
  }

  override fun updateArray(columnLabel: String, x: Array) {
    throw UnsupportedOperationException()
  }

  override fun getRowId(columnIndex: Int): RowId {
    throw UnsupportedOperationException()
  }

  override fun getRowId(columnLabel: String): RowId {
    throw UnsupportedOperationException()
  }

  override fun updateRowId(columnIndex: Int, x: RowId) {
    throw UnsupportedOperationException()
  }

  override fun updateRowId(columnLabel: String, x: RowId) {
    throw UnsupportedOperationException()
  }

  override fun getHoldability(): Int {
    throw UnsupportedOperationException()
  }

  override fun isClosed(): Boolean {
    throw UnsupportedOperationException()
  }

  override fun updateNString(columnIndex: Int, nString: String) {
    throw UnsupportedOperationException()
  }

  override fun updateNString(columnLabel: String, nString: String) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnIndex: Int, nClob: NClob) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnLabel: String, nClob: NClob) {
    throw UnsupportedOperationException()
  }

  override fun getNClob(columnIndex: Int): NClob {
    throw UnsupportedOperationException()
  }

  override fun getNClob(columnLabel: String): NClob {
    throw UnsupportedOperationException()
  }

  override fun getSQLXML(columnLabel: String): SQLXML {
    throw UnsupportedOperationException()
  }

  override fun updateSQLXML(columnIndex: Int, xmlObject: SQLXML) {
    throw UnsupportedOperationException()
  }

  override fun updateSQLXML(columnLabel: String, xmlObject: SQLXML) {
    throw UnsupportedOperationException()
  }

  override fun getNString(columnIndex: Int): String {
    throw UnsupportedOperationException()
  }

  override fun getNString(columnLabel: String): String {
    throw UnsupportedOperationException()
  }

  override fun getNCharacterStream(columnIndex: Int): Reader {
    throw UnsupportedOperationException()
  }

  override fun getNCharacterStream(columnLabel: String): Reader {
    throw UnsupportedOperationException()
  }

  override fun updateNCharacterStream(columnIndex: Int, x: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateNCharacterStream(columnLabel: String, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnIndex: Int, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnIndex: Int, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnIndex: Int, x: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnLabel: String, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnLabel: String, x: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnLabel: String, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnIndex: Int, inputStream: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnLabel: String, inputStream: InputStream, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnIndex: Int, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnLabel: String, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnIndex: Int, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnLabel: String, reader: Reader, length: Long) {
    throw UnsupportedOperationException()
  }

  override fun updateNCharacterStream(columnIndex: Int, x: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateNCharacterStream(columnLabel: String, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnIndex: Int, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnIndex: Int, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnIndex: Int, x: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateAsciiStream(columnLabel: String, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateBinaryStream(columnLabel: String, x: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateCharacterStream(columnLabel: String, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnIndex: Int, inputStream: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateBlob(columnLabel: String, inputStream: InputStream) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateClob(columnLabel: String, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnIndex: Int, reader: Reader) {
    throw UnsupportedOperationException()
  }

  override fun updateNClob(columnLabel: String, reader: Reader) {
    throw UnsupportedOperationException()
  }

  // JRE 1.7
  override fun <T> getObject(columnIndex: Int, type: Class<T>): T {
    return type.cast(getObject(columnIndex))
  }

  override fun <T> getObject(columnLabel: String, type: Class<T>): T {
    return type.cast(getObject(columnLabel))
  }
}
