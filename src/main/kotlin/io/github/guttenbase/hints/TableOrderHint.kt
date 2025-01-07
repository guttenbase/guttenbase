package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableOrderComparatorFactory
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Determine order of tables during copying/comparison.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.schema.comparison.SchemaComparatorTool] to determine table order
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool] to determine table order
 * Hint is used by [io.github.guttenbase.tools.CheckEqualTableDataTool] to determine table order
 *
 * @author M. Dahm
 */
abstract class TableOrderHint : ConnectorHint<TableOrderComparatorFactory> {
  override val connectorHintType = TableOrderComparatorFactory::class.java

  companion object {
    /**
     * Helper method
     */
    @JvmStatic
    fun getSortedTables(connectorRepository: ConnectorRepository, connectorId: String): List<TableMetaData> {
      val databaseMetaData = connectorRepository.getDatabaseMetaData(connectorId)
      val comparator = connectorRepository.hint<TableOrderComparatorFactory>(connectorId).createComparator()

      return databaseMetaData.tableMetaData.sortedWith(comparator)
    }
  }
}
