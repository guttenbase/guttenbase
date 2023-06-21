package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.connector.Connector
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.SelectStatementCreator
import java.sql.Connection
import java.sql.ResultSet
import java.sql.SQLException

/**
 * Read data from given table and put into a List of maps where each map contains the columns and values of a single line from
 * the table.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class ReadTableDataTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String,
  private val tableMetaData: TableMetaData
) {
  private var connector: Connector? = null
  private var resultSet: ResultSet? = null

  @Throws(SQLException::class)
  fun start() {
    if (connector == null) {
      val sourceConfiguration = connectorRepository.getSourceDatabaseConfiguration(connectorId)
      connector = connectorRepository.createConnector(connectorId)

      val connection: Connection = connector!!.openConnection()
      sourceConfiguration.initializeSourceConnection(connection, connectorId)
      val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
      val databaseMetaData = connectorRepository.getDatabaseMetaData(connectorId)
      val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, databaseMetaData)
      val selectStatement = SelectStatementCreator(connectorRepository, connectorId)
        .createSelectStatement(connection, tableName, tableMetaData)

      selectStatement.fetchSize = 512
      sourceConfiguration.beforeSelect(connection, connectorId, tableMetaData)
      resultSet = selectStatement.executeQuery()
      sourceConfiguration.afterSelect(connection, connectorId, tableMetaData)
    }
  }

  @Throws(SQLException::class)
  fun end() {
    if (connector != null) {
      val sourceConfiguration: SourceDatabaseConfiguration = connectorRepository.getSourceDatabaseConfiguration(connectorId)
      val connection: Connection = connector!!.openConnection()
      sourceConfiguration.finalizeSourceConnection(connection, connectorId)
      resultSet!!.close()
      connector!!.closeConnection()
      connector = null
      resultSet = null
    }
  }

  /**
   * @param noLines -1 means read all lines
   * @return list of maps containing table data or null of no more data is available
   */
  @Throws(SQLException::class)
  fun readTableData(noLines: Int): List<Map<String, Any?>>? {
    var lines = noLines
    val result: MutableList<Map<String, Any?>> = ArrayList()
    val commonColumnTypeResolver = CommonColumnTypeResolverTool(connectorRepository)
    val sourceColumnNameMapper: ColumnMapper =
      connectorRepository.getConnectorHint(connectorId, ColumnMapper::class.java).value
    val orderedSourceColumns: List<ColumnMetaData> = ColumnOrderHint.getSortedColumns(
      connectorRepository, connectorId, tableMetaData
    )

    if (lines < 0) {
      lines = Int.MAX_VALUE
    }

    var rowIndex = 0

    while (rowIndex < lines && resultSet?.next() ?: throw IllegalStateException("No ResultSet")) {
      val rowData = HashMap<String, Any?>()

      for (columnIndex in 1..orderedSourceColumns.size) {
        val sourceColumn: ColumnMetaData = orderedSourceColumns[columnIndex - 1]
        val columnName: String = sourceColumnNameMapper.mapColumnName(sourceColumn, tableMetaData)
        val sourceColumnType = commonColumnTypeResolver.getColumnType(connectorId, sourceColumn)
        val columnTypeMapping = commonColumnTypeResolver.getCommonColumnTypeMapping(
          sourceColumn, connectorId, sourceColumn
        ) ?: throw IllegalStateException("Type mapping not found for $sourceColumn")
        val data = sourceColumnType.getValue(resultSet!!, columnIndex)
        val mappedData = columnTypeMapping.columnDataMapper.map(sourceColumn, sourceColumn, data)

        rowData[columnName] = mappedData
      }

      result.add(rowData)
      rowIndex++
    }

    return if (rowIndex == 0) null else result
  }
}
