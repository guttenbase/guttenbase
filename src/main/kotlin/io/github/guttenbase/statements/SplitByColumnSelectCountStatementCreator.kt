package io.github.guttenbase.statements

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.SplitColumn

/**
 * Sometimes the amount of data in the result set exceeds any buffer. In these cases we need to split the data by some given range, usually
 * the primary key. I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 * With this statement we count the number of rows that actually will be read for the given chunk.
 *
 * &copy; 2012-2044 tech@spree
 *
 * Hint is used by [io.github.guttenbase.hints.SplitColumnHint]
 *
 * @author M. Dahm
 */
class SplitByColumnSelectCountStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  override fun createColumnClause(columns: List<ColumnMetaData>) = "COUNT(*)"

  override fun createWhereClause(metaData: DatabaseEntityMetaData): String {
    val splitColumn = connectorRepository.hint<SplitColumn>(targetConnectorId).getSplitColumn(metaData as TableMetaData)

    return "WHERE " + metaData.databaseType.escapeDatabaseEntity(splitColumn.columnName) + " BETWEEN ? AND ?"
  }
}
