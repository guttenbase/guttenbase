package io.github.guttenbase.statements

import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.ColumnOrderHint.Companion.getSortedColumns
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import org.slf4j.Logger
import org.slf4j.LoggerFactory

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
  protected val columnMapper = connectorRepository.getConnectorHint(connectorId, ColumnMapper::class.java).value

  protected open fun createColumnClause(columns: List<ColumnMetaData>) =
    columns.joinToString(separator = ", ", transform = { columnMapper.mapColumnName(it, it.tableMetaData) })

  protected open fun createWhereClause(tableMetaData: TableMetaData): String = ""

  /**
   * Get the list of target columns with appropriate mappings as defined by [io.github.guttenbase.hints.ColumnMapperHint]
   */
  fun getMappedTargetColumns(
    sourceTableMetaData: TableMetaData,
    targetTableMetaData: TableMetaData, sourceConnectorId: String
  ): List<ColumnMetaData> {
    // Use same order as in SELECT clause
    val sourceColumns = getSortedColumns(connectorRepository, sourceConnectorId, sourceTableMetaData)
    val columnMapper = connectorRepository.getConnectorHint(connectorId, ColumnMapper::class.java).value

    return sourceColumns.map {
      val mapping = columnMapper.map(it, targetTableMetaData)
      mapping.columns
    }.flatten()
  }

  companion object {
    @JvmStatic
    protected val LOG: Logger = LoggerFactory.getLogger(AbstractStatementCreator::class.java)
  }
}
