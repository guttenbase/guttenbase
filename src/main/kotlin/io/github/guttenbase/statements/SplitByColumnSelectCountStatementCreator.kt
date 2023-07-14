package io.github.guttenbase.statements

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.SplitColumn


/**
 * Sometimes the amount of data in the result set exceeds any buffer. In these cases we need to split the data by some given range, usually
 * the primary key. I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 *
 * With this statement we count the number of rows that actually will be read for the given chunk.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.hints.SplitColumnHint]
 *
 * @author M. Dahm
 */
class SplitByColumnSelectCountStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  override fun createColumnClause(columns: List<ColumnMetaData>) = "COUNT(*)"

  override fun createWhereClause(tableMetaData: TableMetaData): String {
    val splitColumn: ColumnMetaData = connectorRepository.getConnectorHint(connectorId, SplitColumn::class.java).value
      .getSplitColumn(tableMetaData)
    return "WHERE " + splitColumn.columnName + " BETWEEN ? AND ?"
  }
}
