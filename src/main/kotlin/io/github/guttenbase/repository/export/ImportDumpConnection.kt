package io.github.guttenbase.repository.export

import io.github.guttenbase.exceptions.ImportException
import io.github.guttenbase.meta.TableMetaData
import java.sql.*
import java.util.*
import java.util.concurrent.Executor

/**
 * Special "[Connection]" that supports reading data from a stream.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ImportDumpConnection(private val importer: Importer, private val databaseMetaData: DatabaseMetaData) : Connection {
  private var closed = false
  private val importedTables = HashSet<TableMetaData>()
  private lateinit var currentTableMetaData: TableMetaData

  fun initializeReadTable(table: TableMetaData) {
    currentTableMetaData = table
  }

  fun finalizeReadTable(table: TableMetaData) {
  }

  /**
   * Returns custom PreparedStatement statement. {@inheritDoc}
   */
  override fun prepareStatement(sql: String): PreparedStatement {
    // Read header only once
    if (importedTables.add(currentTableMetaData)) {
      seekTableHeader(currentTableMetaData)
    }

    return ImportDumpPreparedStatement(importer, databaseMetaData, currentTableMetaData, sql)
  }

  @Throws(ImportException::class)
  private fun seekTableHeader(tableMetaData: TableMetaData?) {
    try {
      importer.seekTableHeader(tableMetaData)
    } catch (e: Exception) {
      throw ImportException("getObject", e)
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun close() {
    try {
      importer.finishImport()
    } catch (e: Exception) {
      throw ImportException("close", e)
    } finally {
      closed = true
      importedTables.clear()
    }
  }

  override fun isClosed() = closed

  override fun commit() {}
  override fun setAutoCommit(autoCommit: Boolean) {}
  override fun getAutoCommit(): Boolean {
    return false
  }

  override fun rollback() {}
  override fun setReadOnly(readOnly: Boolean) {}
  override fun isReadOnly(): Boolean {
    return true
  }

  override fun createStatement(): Statement {
    return prepareStatement("")
  }

  override fun <T> unwrap(iface: Class<T>): T {
    throw UnsupportedOperationException()
  }

  override fun isWrapperFor(iface: Class<*>?): Boolean {
    throw UnsupportedOperationException()
  }

  override fun prepareCall(sql: String): CallableStatement {
    throw UnsupportedOperationException()
  }

  override fun nativeSQL(sql: String): String {
    throw UnsupportedOperationException()
  }

  override fun getMetaData(): DatabaseMetaData {
    throw UnsupportedOperationException()
  }

  override fun setCatalog(catalog: String) {
    throw UnsupportedOperationException()
  }

  override fun getCatalog(): String {
    throw UnsupportedOperationException()
  }

  override fun setTransactionIsolation(level: Int) {
    throw UnsupportedOperationException()
  }

  override fun getTransactionIsolation(): Int {
    return Connection.TRANSACTION_NONE
  }

  override fun getWarnings(): SQLWarning {
    throw UnsupportedOperationException()
  }

  override fun clearWarnings() {
    throw UnsupportedOperationException()
  }

  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement {
    return createStatement()
  }

  override fun prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement {
    return prepareStatement(sql)
  }

  override fun prepareCall(sql: String, resultSetType: Int, resultSetConcurrency: Int): CallableStatement {
    throw UnsupportedOperationException()
  }

  override fun getTypeMap(): Map<String, Class<*>> {
    throw UnsupportedOperationException()
  }

  override fun setTypeMap(map: Map<String?, Class<*>?>?) {
    throw UnsupportedOperationException()
  }

  override fun setHoldability(holdability: Int) {
    throw UnsupportedOperationException()
  }

  override fun getHoldability(): Int {
    return 0
  }

  override fun setSavepoint(): Savepoint {
    throw UnsupportedOperationException()
  }

  override fun setSavepoint(name: String): Savepoint {
    throw UnsupportedOperationException()
  }

  override fun rollback(savepoint: Savepoint) {
    throw UnsupportedOperationException()
  }

  override fun releaseSavepoint(savepoint: Savepoint) {
    throw UnsupportedOperationException()
  }

  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int): Statement {
    throw UnsupportedOperationException()
  }

  override fun prepareStatement(
    sql: String, resultSetType: Int, resultSetConcurrency: Int,
    resultSetHoldability: Int
  ): PreparedStatement {
    throw UnsupportedOperationException()
  }

  override fun prepareCall(
    sql: String, resultSetType: Int, resultSetConcurrency: Int,
    resultSetHoldability: Int
  ): CallableStatement {
    throw UnsupportedOperationException()
  }

  override fun prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement {
    throw UnsupportedOperationException()
  }

  override fun prepareStatement(sql: String, columnIndexes: IntArray): PreparedStatement {
    throw UnsupportedOperationException()
  }

  override fun prepareStatement(sql: String, columnNames: Array<String>): PreparedStatement {
    throw UnsupportedOperationException()
  }

  override fun createClob(): Clob {
    throw UnsupportedOperationException()
  }

  override fun createBlob(): Blob {
    throw UnsupportedOperationException()
  }

  override fun createNClob(): NClob {
    throw UnsupportedOperationException()
  }

  override fun createSQLXML(): SQLXML {
    throw UnsupportedOperationException()
  }

  override fun isValid(timeout: Int): Boolean {
    throw UnsupportedOperationException()
  }

  @Throws(SQLClientInfoException::class)
  override fun setClientInfo(name: String, value: String) {
    throw UnsupportedOperationException()
  }

  @Throws(SQLClientInfoException::class)
  override fun setClientInfo(properties: Properties) {
    throw UnsupportedOperationException()
  }

  override fun getClientInfo(name: String): String {
    throw UnsupportedOperationException()
  }

  override fun getClientInfo(): Properties {
    throw UnsupportedOperationException()
  }

  override fun createArrayOf(typeName: String, elements: Array<Any>): java.sql.Array {
    throw UnsupportedOperationException()
  }

  override fun createStruct(typeName: String, attributes: Array<Any>): Struct {
    throw UnsupportedOperationException()
  }

  // JRE 1.7
  override fun setSchema(schema: String) {
    throw UnsupportedOperationException()
  }

  override fun getSchema(): String {
    throw UnsupportedOperationException()
  }

  override fun abort(executor: Executor) {
    throw UnsupportedOperationException()
  }

  override fun setNetworkTimeout(executor: Executor, milliseconds: Int) {
    throw UnsupportedOperationException()
  }

  override fun getNetworkTimeout(): Int {
    throw UnsupportedOperationException()
  }
}
