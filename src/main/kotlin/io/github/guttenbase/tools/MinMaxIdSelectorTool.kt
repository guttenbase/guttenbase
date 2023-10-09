package io.github.guttenbase.tools

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.SplitByColumnSelectMinMaxStatementCreator
import java.sql.Connection
import java.sql.SQLException

/**
 * Compute MIN and MAX of given Id-Column
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class MinMaxIdSelectorTool(private val connectorRepository: ConnectorRepository) {
  var minValue: Long = 0
    private set
  var maxValue: Long = 0
    private set

  /**
   * Compute MIN and MAX of given Id-Column
   */
  @Throws(SQLException::class)
  fun computeMinMax(connectorId: String, tableMetaData: TableMetaData) {
    val connector = connectorRepository.createConnector(connectorId)
    val connection: Connection = connector.openConnection()

    computeMinMax(connectorId, tableMetaData, connection)
    connector.closeConnection()
  }

  /**
   * Compute MIN and MAX of given Id-Column using existing connection
   */
  @Throws(SQLException::class)
  fun computeMinMax(connectorId: String, tableMetaData: TableMetaData, connection: Connection) {
    val tableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)
    val minMaxStatement = SplitByColumnSelectMinMaxStatementCreator(connectorRepository, connectorId)
      .createSelectStatement(connection, tableName, tableMetaData)
    val rangeResultSet = minMaxStatement.executeQuery()

    rangeResultSet.next()
    minValue = rangeResultSet.getLong(1)
    maxValue = rangeResultSet.getLong(2)
    minMaxStatement.close()
  }
}
