package io.github.guttenbase.tools

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.statements.AbstractInsertStatementCreator
import java.sql.PreparedStatement

@Suppress("MemberVisibilityCanBePrivate")
class InsertStatementTool(connectorRepository: ConnectorRepository, targetConnectorId: String) :
  AbstractInsertStatementCreator(connectorRepository, targetConnectorId), AutoCloseable {
  private var parameters: Int = -1
  private lateinit var statement: PreparedStatement
  private lateinit var columns: List<ColumnMetaData>
  private val columnMap: Map<String, ColumnMetaData> by lazy { columns.associateBy { it.columnName.lowercase() } }

  @JvmOverloads
  fun createInsertStatementSQL(tableName: String, includePrimaryKey: Boolean = true): String {
    val table = connectorRepository.getDatabase(this@InsertStatementTool.targetConnectorId).getTable(tableName)
      ?: throw IllegalStateException("Table $tableName not found")
    columns = getMappedTargetColumns(table, table)
      .filter { if (!includePrimaryKey) !it.isPrimaryKey else true }

    if (columns.isEmpty()) {
      throw IllegalStateException("No matching columns for $tableName")
    }

    return "INSERT INTO " + table.getTableName() + " (" + createColumnClause(columns) + ") VALUES\n" +
        createValueTuples(1, columns)
  }

  private fun TableMetaData.getTableName(): String {
    val targetDatabaseMetaData = connectorRepository.getDatabase(targetConnectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)

    return tableMapper.fullyQualifiedTableName(this, targetDatabaseMetaData)
  }

  @JvmOverloads
  fun createInsertStatement(
    tableName: String,
    includePrimaryKey: Boolean = true,
  ): InsertStatementTool {
    val connection = connectorRepository.createConnector(this@InsertStatementTool.targetConnectorId).openConnection()

    statement = connection.prepareStatement(createInsertStatementSQL(tableName, includePrimaryKey))
    resetParameters()

    return this
  }

  fun setParameter(columnName: String, value: Any?): InsertStatementTool {
    val column = columnMap[columnName.lowercase()] ?: throw IllegalStateException("Table $columnName not found")
    val columnIndex = columns.indexOfFirst { it.columnName.lowercase() == columnName.lowercase() } + 1
    val mapping = ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(column, column)
      ?: throw IllegalStateException("Type mapping not found for $column")

    mapping.sourceColumnType.setValue(statement, columnIndex, column, value)

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

