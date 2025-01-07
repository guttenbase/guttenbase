package io.github.guttenbase.mapping

import io.github.guttenbase.meta.TableMetaData

/**
 * Determine order of tables during copying/comparison.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 */
fun interface TableOrderComparatorFactory {
  fun createComparator(): Comparator<TableMetaData>
}
