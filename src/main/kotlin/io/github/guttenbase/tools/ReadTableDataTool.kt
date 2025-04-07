package io.github.guttenbase.tools

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.statements.SelectStatementCreator
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

/**
 * Read data from given table and put into a List of maps where each map contains the columns and values of a single line from
 * the table.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class ReadTableDataTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String,
  private val tableMetaData: DatabaseEntityMetaData,
) : AutoCloseable {
  constructor(connectorRepository: ConnectorRepository, connectorId: String, tableName: String, view: Boolean = false) : this(
    connectorRepository, connectorId,
    if (view)
      connectorRepository.getDatabase(connectorId).getView(tableName) ?: throw IllegalStateException("View $tableName not found")
    else
      connectorRepository.getDatabase(connectorId).getTable(tableName) ?: throw IllegalStateException("Table $tableName not found")
  )

  private lateinit var connector: Connector
  private lateinit var resultSet: ResultSet

  fun start(): ReadTableDataTool {
    if (!this::connector.isInitialized) {
      connector = connectorRepository.createConnector(connectorId)

      start(connector.openConnection())
    }

    return this
  }

  fun start(connection: Connection): ReadTableDataTool {
    val sourceConfiguration = connectorRepository.getSourceDatabaseConfiguration(connectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val databaseMetaData = connectorRepository.getDatabase(connectorId)
    val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, databaseMetaData)
    val selectStatement = SelectStatementCreator(connectorRepository, connectorId)
      .createSelectStatement(connection, tableName, tableMetaData)

    selectStatement.fetchSize = 512
    sourceConfiguration.beforeSelect(connection, connectorId, tableMetaData)
    resultSet = selectStatement.executeQuery()
    sourceConfiguration.afterSelect(connection, connectorId, tableMetaData)

    return this
  }

  fun end() {
    if (this::connector.isInitialized) {
      end(connector.openConnection())
      connector.closeConnection()
    }
  }

  fun end(connection: Connection) {
    val sourceConfiguration = connectorRepository.getSourceDatabaseConfiguration(connectorId)
    sourceConfiguration.finalizeSourceConnection(connection, connectorId)
    resultSet.close()
  }

  /**
   * @param noLines -1 means read all lines
   * @return list of maps containing table data or null of no more data is available
   */
  fun readTableData(noLines: Int): List<Map<String, Any?>> {
    var lines = noLines
    val result = ArrayList<Map<String, Any?>>()
    val sourceColumnNameMapper = connectorRepository.hint<ColumnMapper>(connectorId)
    val orderedSourceColumns = ColumnOrderHint.getSortedColumns(connectorRepository, tableMetaData)

    if (lines < 0) {
      lines = Int.MAX_VALUE
    }

    var rowIndex = 0

    while (rowIndex < lines && resultSet.next()) {
      val rowData = TreeMap<String, Any?>()

      for (columnIndex in 1..orderedSourceColumns.size) {
        val sourceColumn = orderedSourceColumns[columnIndex - 1]
        val columnName = sourceColumnNameMapper.mapColumnName(sourceColumn, tableMetaData)
        val columnMapping = ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(
          sourceColumn, sourceColumn
        ) ?: throw IllegalStateException("Type mapping not found for $sourceColumn")
        val data = columnMapping.sourceColumnType.getValue(resultSet, columnIndex, sourceColumn)
        val mappedData = columnMapping.columnDataMapper.map(columnMapping, data)

        rowData[columnName] = mappedData
      }

      result.add(rowData)
      rowIndex++
    }

    return result
  }

  override fun close() {
    end()
  }
}
