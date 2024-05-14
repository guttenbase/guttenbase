package io.github.guttenbase.statements

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.ResultSetParameters
import io.github.guttenbase.tools.SelectWhereClause
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import kotlin.math.min

/**
 * Create SELECT statement for copying data.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractSelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractStatementCreator(connectorRepository, connectorId) {
  /**
   * Create SELECT statement in the source table to retrieve data from the configured source columns.
   */
  @Throws(SQLException::class)
  fun createSelectStatement(
    connection: Connection,
    tableName: String,
    tableMetaData: TableMetaData
  ): PreparedStatement {
    val resultSetParameters = connectorRepository.hint<ResultSetParameters>(connectorId)
    val columns = ColumnOrderHint.getSortedColumns(connectorRepository, connectorId, tableMetaData)
    val sql = createSQL(tableName, tableMetaData, columns)

    LOG.debug("Create SELECT statement: $sql")
    val preparedStatement = connection.prepareStatement(
      sql,
      resultSetParameters.getResultSetType(tableMetaData),
      resultSetParameters.getResultSetConcurrency(tableMetaData)
    ).apply {
      fetchSize = min(resultSetParameters.getFetchSize(tableMetaData), maxRows)
    }

    return preparedStatement
  }

  override fun createWhereClause(tableMetaData: TableMetaData) =
    connectorRepository.hint<SelectWhereClause>(connectorId).getWhereClause(tableMetaData)

  /**
   * Create SELECT statement in the target table to retrieve data from the mapped columns. I.e., since the target table
   * configuration may be different, the SELECT statement may be different as well.
   *
   * This is used to check data compatibility by the [io.github.guttenbase.tools.CheckEqualTableDataTool]
   */
  @Throws(SQLException::class)
  fun createMappedSelectStatement(
    connection: Connection, sourceTableMetaData: TableMetaData, tableName: String,
    targetTableMetaData: TableMetaData, sourceConnectorId: String, targetConnectorId: String
  ): PreparedStatement {
    val resultSetParameters =
      connectorRepository.hint<ResultSetParameters>(targetConnectorId)
    val columns = getMappedTargetColumns(sourceTableMetaData, targetTableMetaData, sourceConnectorId)
    val sql = createSQL(tableName, targetTableMetaData, columns)
    LOG.debug("Create mapped SELECT statement: $sql")

    return connection.prepareStatement(
      sql,
      resultSetParameters.getResultSetType(targetTableMetaData),
      resultSetParameters.getResultSetConcurrency(targetTableMetaData)
    )
  }

  /**
   * Retrieve data in some deterministic order
   */
  protected open fun createOrderBy(tableMetaData: TableMetaData) = ""

  private fun createSQL(tableName: String, tableMetaData: TableMetaData, columns: List<ColumnMetaData>) =
    "SELECT " + createColumnClause(columns) +
        " FROM " + tableName +
        " " + createWhereClause(tableMetaData) +
        " " + createOrderBy(tableMetaData)
}
