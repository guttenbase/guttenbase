package io.github.guttenbase.statements

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ResultSetParameters
import io.github.guttenbase.tools.SelectWhereClause
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import kotlin.math.min


/**
 * Create SELECT statement for copying data.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractSelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractStatementCreator(connectorRepository, connectorId) {
  /**
   * Create SELECT statement in the source table to retrieve data from the configured source columns.
   */
  @Throws(SQLException::class)
  fun createSelectStatement(connection: Connection, tableName: String, tableMetaData: TableMetaData): PreparedStatement {
    val resultSetParameters = connectorRepository.getConnectorHint(connectorId, ResultSetParameters::class.java).value
    val columns: List<ColumnMetaData> = ColumnOrderHint.getSortedColumns(connectorRepository, connectorId, tableMetaData)
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
    connectorRepository.getConnectorHint(connectorId, SelectWhereClause::class.java).value.getWhereClause(tableMetaData)

  /**
   * Create SELECT statement in the target table to retrieve data from the mapped columns. I.e., since the target table
   * configuration may be different, the SELECT statement may be different. This is needed to check data compatibility with the
   * [io.github.guttenbase.tools.CheckEqualTableDataTool]
   */
  @Throws(SQLException::class)
  fun createMappedSelectStatement(
    connection: Connection, sourceTableMetaData: TableMetaData, tableName: String,
    targetTableMetaData: TableMetaData, sourceConnectorId: String
  ): PreparedStatement {
    val columns: List<ColumnMetaData> = getMappedTargetColumns(sourceTableMetaData, targetTableMetaData, sourceConnectorId)
    val sql = createSQL(tableName, targetTableMetaData, columns)

    return connection.prepareStatement(sql)
  }

  /**
   * Try to retrieve data in some deterministic order
   */
  protected open fun createOrderBy(tableMetaData: TableMetaData) = ""

  private fun createSQL(tableName: String, tableMetaData: TableMetaData, columns: List<ColumnMetaData>) =
    "SELECT " + createColumnClause(columns) +
        FROM + tableName +
        " " + createWhereClause(tableMetaData) +
        " " + createOrderBy(tableMetaData)

  companion object {
    const val FROM = " FROM "
  }
}
