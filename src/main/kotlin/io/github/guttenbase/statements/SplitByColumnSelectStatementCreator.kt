package io.github.guttenbase.statements

import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.SplitColumn

/**
 * Sometimes the amount of data exceeds any buffer. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.hints.SplitColumnHint]
 *
 * @author M. Dahm
 */
class SplitByColumnSelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  override fun createWhereClause(table: DatabaseEntityMetaData): String {
    val splitColumn = connectorRepository.hint<SplitColumn>(targetConnectorId).getSplitColumn(table as TableMetaData)
    val columnName = table.databaseType.escapeDatabaseEntity(splitColumn.columnName)

    return "WHERE $columnName BETWEEN ? AND ?"
  }

  override fun createOrderBy(table: DatabaseEntityMetaData): String {
    val splitColumn = connectorRepository.hint<SplitColumn>(targetConnectorId).getSplitColumn(table as TableMetaData)
    val columnName = table.databaseType.escapeDatabaseEntity(splitColumn.columnName)

    return "ORDER BY $columnName"
  }
}
