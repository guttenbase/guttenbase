package io.github.guttenbase.tools

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.impl.ClassNameColumnTypeResolver
import io.github.guttenbase.statements.AbstractInsertStatementCreator
import java.sql.PreparedStatement

@Suppress("MemberVisibilityCanBePrivate")
class InsertStatementTool(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractInsertStatementCreator(connectorRepository, connectorId), AutoCloseable {
  private var parameters: Int = -1
  private lateinit var statement: PreparedStatement
  private lateinit var columns: List<ColumnMetaData>
  private val columnMap: Map<String, ColumnMetaData> by lazy { columns.associateBy { it.columnName.lowercase() } }
  private val database: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(connectorId) }

  @JvmOverloads
  fun createInsertStatementSQL(
    tableName: String,
    includePrimaryKey: Boolean = true,
  ): String {
    val tableMetaData = connectorRepository.getDatabaseMetaData(connectorId).getTableMetaData(tableName)
      ?: throw IllegalStateException("Table $tableName not found")
    columns = getMappedTargetColumns(tableMetaData, tableMetaData, connectorId)
      .filter { if (!includePrimaryKey) !it.isPrimaryKey else true }

    return "INSERT INTO " + tableName + " (" + createColumnClause(columns) + ") VALUES " +
        createValueTuples(1, columns.size)
  }

  @JvmOverloads
  fun createInsertStatement(
    tableName: String,
    includePrimaryKey: Boolean = true,
  ): InsertStatementTool {
    val connection = connectorRepository.createConnector(connectorId).openConnection()

    statement = connection.prepareStatement(createInsertStatementSQL(tableName, includePrimaryKey))
    resetParameters()

    return this
  }

  fun setParameter(columnName: String, value: Any?): InsertStatementTool {
    val columnMetaData = columnMap[columnName.lowercase()] ?: throw IllegalStateException("Table $columnName not found")
    val columnIndex = columns.indexOfFirst { it.columnName.lowercase() == columnName.lowercase() } + 1
    val columnType = ClassNameColumnTypeResolver.getColumnType(columnMetaData)

    columnType.setValue(statement, columnIndex, database, columnMetaData.columnType, value)

    return this
  }

  fun resetParameters() {
    parameters = 1
  }

  override fun close() {
    if (this::statement.isInitialized && !statement.connection.isClosed) {
      statement.connection.close()
    }

    parameters = -1
  }

  fun execute() {
    statement.execute()
  }
}

