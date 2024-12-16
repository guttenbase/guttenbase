package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnOrderComparatorFactory
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Determine order of columns in SELECT statement. This will of course also influence the ordering of the resulting INSERT statement.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.statements.AbstractStatementCreator] to determine column order
 * Hint is used by [io.github.guttenbase.statements.InsertStatementFiller] to determine column order
 * Hint is used by [io.github.guttenbase.tools.CheckEqualTableDataTool] to determine column order
 *
 * @author M. Dahm
 */
abstract class ColumnOrderHint : ConnectorHint<ColumnOrderComparatorFactory> {
  override val connectorHintType: Class<ColumnOrderComparatorFactory>
    get() = ColumnOrderComparatorFactory::class.java

  companion object {
    /**
     * Helper method
     */
    @JvmStatic
    fun getSortedColumns(connectorRepository: ConnectorRepository, connectorId: String, tableMetaData: TableMetaData)
        : List<ColumnMetaData> {
      val sourceColumnComparator =
        connectorRepository.hint<ColumnOrderComparatorFactory>(connectorId).createComparator()

      return tableMetaData.columnMetaData.sortedWith(sourceColumnComparator)
    }
  }
}
