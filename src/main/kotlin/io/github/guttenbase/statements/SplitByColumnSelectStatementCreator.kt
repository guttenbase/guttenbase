package io.github.guttenbase.statements

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.SplitColumn

/**
 * Sometimes the amount of data exceeds any buffer. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [SplitColumnHint]
 *
 * @author M. Dahm
 */
class SplitByColumnSelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  override fun createWhereClause(tableMetaData: TableMetaData): String {
    val splitColumn: ColumnMetaData = connectorRepository.getConnectorHint(connectorId, SplitColumn::class.java).value
      .getSplitColumn(tableMetaData)

    return "WHERE " + splitColumn.columnName + " BETWEEN ? AND ?"
  }

  override fun createOrderBy(tableMetaData: TableMetaData): String {
    val splitColumn: ColumnMetaData = connectorRepository.getConnectorHint(connectorId, SplitColumn::class.java).value
      .getSplitColumn(tableMetaData)

    return "ORDER BY " + splitColumn.columnName
  }
}
