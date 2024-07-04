package io.github.guttenbase.statements

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.ColumnOrderHint.Companion.getSortedColumns
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.progress.TableCopyProgressIndicator
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Contains some helper methods for implementing classes.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.ColumnMapperHint] to map column names
 * Hint is used by [ColumnOrderHint] to determine column order
 */
abstract class AbstractStatementCreator(
  protected val connectorRepository: ConnectorRepository,
  protected val connectorId: String
) {
  protected val columnMapper = connectorRepository.hint<ColumnMapper>(connectorId)
  protected val indicator = connectorRepository.hint<TableCopyProgressIndicator>(connectorId)

  protected open fun createColumnClause(columns: List<ColumnMetaData>) =
    columns.joinToString(separator = ", ", transform = { columnMapper.mapColumnName(it, it.tableMetaData) })

  protected open fun createWhereClause(tableMetaData: TableMetaData): String = ""

  /**
   * Get the list of target columns with appropriate mappings as defined by [io.github.guttenbase.hints.ColumnMapperHint]
   */
  fun getMappedTargetColumns(
    sourceTableMetaData: TableMetaData, targetTableMetaData: TableMetaData, sourceConnectorId: String
  ): List<ColumnMetaData> {
    // Use same order as in SELECT clause
    val sourceColumns = getSortedColumns(connectorRepository, sourceConnectorId, sourceTableMetaData)
    val columnMapper = connectorRepository.hint<ColumnMapper>(connectorId)

    return sourceColumns.map {
      val mapping = columnMapper.map(it, targetTableMetaData)
      mapping.columns
    }.flatten()
  }
}
