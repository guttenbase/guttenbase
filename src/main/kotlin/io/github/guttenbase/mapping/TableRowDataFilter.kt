package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Filter rows by inspection of data
 *
 *   2022-2034 tech@spree
 *
 */
fun interface TableRowDataFilter {
  fun accept(sourceValues: Map<ColumnMetaData, Any?>, targetValues: Map<ColumnMetaData, Any?>): Boolean
}
