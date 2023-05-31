package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Determine order of columns in SELECT/INSERT statement.
 *
 *  2012-2034 akquinet tech@spree
 *
 */
fun interface ColumnOrderComparatorFactory {
  fun createComparator(): Comparator<ColumnMetaData>
}
