package io.github.guttenbase.export

import io.github.guttenbase.exceptions.ExportException
import io.github.guttenbase.meta.TableMetaData
import java.sql.*
import java.util.*
import java.util.concurrent.Executor

/**
 * Special "[Connection]" that supports writing data to a different storage then a data base. I.e., a file dump. Only few
 * inherited methods have a meaningful implementation, most methods will throw a [UnsupportedOperationException].
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpConnection(private val exporter: Exporter) : Connection {
  private var closed = false
  private val exportedTables = HashSet<TableMetaData>()
  private lateinit var currentTableMetaData: TableMetaData

  /**
   * Returns PreparedStatement object for export. {@inheritDoc}
   */
  override fun prepareStatement(sql: String): PreparedStatement {
    // Write header only once
    if (exportedTables.add(currentTableMetaData)) {
      writeTableHeader(currentTableMetaData)
    }

    return ExportDumpPreparedStatement(exporter)
  }

  /**
   * Simply forwards call to [Exporter].
   */
  @Throws(ExportException::class)
  fun initializeWriteTableData(table: TableMetaData) {
    currentTableMetaData = table

    try {
      exporter.initializeWriteTableData(table)
    } catch (e: Exception) {
      throw ExportException("initializeWriteTableData", e)
    }
  }

  /**
   * Simply forwards call to [Exporter].
   */
  @Throws(ExportException::class)
  fun finalizeWriteTableData(table: TableMetaData) {
    try {
      exporter.finalizeWriteTableData(table)
    } catch (e: Exception) {
      throw ExportException("finalizeWriteTableData", e)
    }
  }

  @Throws(ExportException::class)
  fun initializeWriteRowData(table: TableMetaData) {
    try {
      exporter.initializeWriteRowData(table)
    } catch (e: Exception) {
      throw ExportException("initializeWriteRowData", e)
    }
  }

  @Throws(ExportException::class)
  fun finalizeWriteRowData(table: TableMetaData) {
    try {
      exporter.finalizeWriteRowData(table)
    } catch (e: Exception) {
      throw ExportException("finalizeWriteRowData", e)
    }
  }

  /**
   * Simply forwards call to [Exporter] which will close any open resources.
   */
  override fun close() {
    try {
      exporter.finishExport()
    } catch (e: Exception) {
      throw ExportException("close", e)
    } finally {
      closed = true
      exportedTables.clear()
    }
  }

  /**
   * Simply forwards call to [Exporter] which may then flush its buffers.
   */
  override fun commit() {
    if (!isClosed) {
      try {
        exporter.flush()
      } catch (e: Exception) {
        throw ExportException("commit", e)
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun isClosed() = closed

  override fun setAutoCommit(autoCommit: Boolean) {}
  override fun getAutoCommit() = false

  override fun setReadOnly(readOnly: Boolean) {}
  override fun isReadOnly(): Boolean {
    return false
  }

  override fun rollback() {}
  override fun prepareStatement(sql: String, autoGeneratedKeys: Int): PreparedStatement {
    return prepareStatement(sql)
  }

  override fun prepareStatement(sql: String, columnIndexes: IntArray): PreparedStatement {
    return prepareStatement(sql)
  }

  override fun prepareStatement(sql: String, columnNames: Array<String>): PreparedStatement {
    return prepareStatement(sql)
  }

  override fun createStatement(): Statement {
    return prepareStatement("")
  }

  override fun <T> unwrap(iface: Class<T>): T {
    throw UnsupportedOperationException()
  }

  override fun isWrapperFor(iface: Class<*>): Boolean {
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
    throw UnsupportedOperationException()
  }

  override fun getWarnings(): SQLWarning {
    throw UnsupportedOperationException()
  }

  override fun clearWarnings() {}
  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int): Statement {
    throw UnsupportedOperationException()
  }

  override fun prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int): PreparedStatement {
    throw UnsupportedOperationException()
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
    throw UnsupportedOperationException()
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

  @Suppress("RemoveRedundantQualifierName")
  override fun createArrayOf(typeName: String, elements: Array<Any>): java.sql.Array {
    throw UnsupportedOperationException()
  }

  override fun createStruct(typeName: String, attributes: Array<Any>): Struct {
    throw UnsupportedOperationException()
  }

  private fun writeTableHeader(tableMetaData: TableMetaData) {
    try {
      exporter.writeTableHeader(ExportTableHeaderImpl(tableMetaData))
    } catch (e: Exception) {
      throw ExportException("writeTableHeader", e)
    }
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
