package io.github.guttenbase.mapping

import io.github.guttenbase.meta.TableMetaData

/**
 * Determine order of tables during copying/comparison.
 *
 *  2012-2034 akquinet tech@spree
 *
 */
fun interface TableOrderComparatorFactory {
  fun createComparator(): Comparator<TableMetaData>
}
