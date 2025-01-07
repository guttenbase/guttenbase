package io.github.guttenbase.export.plain

import java.io.BufferedWriter
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.sql.*
import java.util.*
import java.util.concurrent.Executor
import java.util.zip.GZIPOutputStream

typealias TO_STRING = (PrintWriter) -> Unit

/**
 * Connection info for exporting data to a file.
 *
 * &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportPlainTextConnection(internal val connector: ExportPlainConnector) : Connection {
  private var closed = false
  private val preparedStatements = ArrayList<ExportPlainTextStatement>()
  internal val printWriter = PrintWriter(BufferedWriter(outputStreamWriter()))

  private fun outputStreamWriter(): OutputStreamWriter {
    val outputStream = if (connector.connectorInfo.compress)
      GZIPOutputStream(connector.connectorInfo.outputStream)
    else
      connector.connectorInfo.outputStream

    return OutputStreamWriter(outputStream, connector.connectorInfo.encoding)
  }

  private fun createStatement(sql: String = ""): ExportPlainTextStatement {
    val result = ExportPlainTextStatement(sql, this)
    preparedStatements.add(result)
    return result
  }

  override fun close() {
    closed = true
    preparedStatements.forEach { it.close() }
    printWriter.close()
  }

  override fun commit() {
  }

  override fun rollback() {
  }

  override fun createStatement() = createStatement("")

  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int) = createStatement("")

  override fun createStatement(resultSetType: Int, resultSetConcurrency: Int, resultSetHoldability: Int) =
    createStatement("")

  override fun prepareStatement(sql: String) = ExportPlainTextStatement(sql, this)

  override fun prepareStatement(sql: String, resultSetType: Int, resultSetConcurrency: Int) = createStatement(sql)

  override fun prepareStatement(
    sql: String,
    resultSetType: Int,
    resultSetConcurrency: Int,
    resultSetHoldability: Int
  ) = createStatement(sql)

  override fun prepareStatement(sql: String, autoGeneratedKeys: Int) = createStatement(sql)

  override fun prepareStatement(sql: String, columnIndexes: IntArray) = createStatement(sql)

  override fun prepareStatement(sql: String, columnNames: Array<out String>) = createStatement(sql)

  override fun setAutoCommit(autoCommit: Boolean) {}

  override fun getAutoCommit(): Boolean = false

  override fun rollback(savepoint: Savepoint) {}

  override fun isClosed() = closed

  override fun setReadOnly(readOnly: Boolean) {}

  override fun isReadOnly() = false


  override fun <T : Any?> unwrap(iface: Class<T>?): T {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun isWrapperFor(iface: Class<*>?) = false

  override fun prepareCall(sql: String?): CallableStatement {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun prepareCall(sql: String?, resultSetType: Int, resultSetConcurrency: Int): CallableStatement {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun prepareCall(
    sql: String?,
    resultSetType: Int,
    resultSetConcurrency: Int,
    resultSetHoldability: Int
  ): CallableStatement {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun nativeSQL(sql: String?): String {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getMetaData(): DatabaseMetaData {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setCatalog(catalog: String?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getCatalog(): String {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setTransactionIsolation(level: Int) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getTransactionIsolation(): Int {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getWarnings(): SQLWarning {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun clearWarnings() {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getTypeMap(): MutableMap<String, Class<*>> {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setTypeMap(map: MutableMap<String, Class<*>>?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setHoldability(holdability: Int) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getHoldability(): Int {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setSavepoint(): Savepoint {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setSavepoint(name: String?): Savepoint {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun releaseSavepoint(savepoint: Savepoint?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun createClob(): Clob {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun createBlob(): Blob {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun createNClob(): NClob {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun createSQLXML(): SQLXML {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun isValid(timeout: Int): Boolean {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setClientInfo(name: String?, value: String?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setClientInfo(properties: Properties?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getClientInfo(name: String?): String {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getClientInfo(): Properties {
    throw UnsupportedOperationException("Not implemented")
  }

  @Suppress("RemoveRedundantQualifierName")
  override fun createArrayOf(typeName: String?, elements: Array<out Any>?): java.sql.Array {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun createStruct(typeName: String?, attributes: Array<out Any>?): Struct {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setSchema(schema: String?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getSchema(): String {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun abort(executor: Executor?) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun setNetworkTimeout(executor: Executor?, milliseconds: Int) {
    throw UnsupportedOperationException("Not implemented")
  }

  override fun getNetworkTimeout(): Int {
    throw UnsupportedOperationException("Not implemented")
  }
}