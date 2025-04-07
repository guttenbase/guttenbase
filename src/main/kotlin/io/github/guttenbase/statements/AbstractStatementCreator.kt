package io.github.guttenbase.statements

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.ColumnOrderHint.Companion.getSortedColumns
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.progress.TableCopyProgressIndicator
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Contains some helper methods for implementing classes.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.ColumnMapperHint] to map column names
 * Hint is used by [ColumnOrderHint] to determine column order
 */
abstract class AbstractStatementCreator(
  protected val connectorRepository: ConnectorRepository,
  protected val targetConnectorId: String
) {
  protected val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
  protected val indicator = connectorRepository.hint<TableCopyProgressIndicator>(targetConnectorId)

  protected open fun createColumnClause(columns: List<ColumnMetaData>) =
    columns.joinToString(separator = ", ", transform = {
      val rawColumnName = columnMapper.mapColumnName(it, it.container)

      it.databaseType.escapeDatabaseEntity(rawColumnName)
    })

  protected open fun createWhereClause(tableMetaData: DatabaseEntityMetaData): String = ""

  /**
   * Get the list of target columns with appropriate mappings as defined by [io.github.guttenbase.hints.ColumnMapperHint]
   */
  fun getMappedTargetColumns(sourceTableMetaData: TableMetaData, targetTableMetaData: TableMetaData): List<ColumnMetaData> {
    // Use same order as in SELECT clause
    val sourceColumns = getSortedColumns(connectorRepository, sourceTableMetaData)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)

    return sourceColumns.map { columnMapper.map(it, targetTableMetaData).columns }.flatten()
  }
}
