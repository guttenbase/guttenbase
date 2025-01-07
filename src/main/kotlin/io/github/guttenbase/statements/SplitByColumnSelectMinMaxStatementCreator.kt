package io.github.guttenbase.statements

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.SplitColumn

/**
 * Sometimes the amount of data exceeds any buffer. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 * Read minimum and maximum value of that column.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.hints.SplitColumnHint]
 *
 * @author M. Dahm
 */
class SplitByColumnSelectMinMaxStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  override fun createColumnClause(columns: List<ColumnMetaData>): String {
    assert(columns.isNotEmpty())

    val tableMetaData: TableMetaData = columns[0].tableMetaData
    val splitColumn: ColumnMetaData = connectorRepository.hint<SplitColumn>(targetConnectorId).getSplitColumn(tableMetaData)

    return "MIN(" + splitColumn.columnName + "), MAX(" + splitColumn.columnName + ")"
  }
}
