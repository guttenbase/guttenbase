package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultColumnComparator
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.mapping.ColumnOrderComparatorFactory

/**
 * By default order by natural order of column names.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnOrderHint : ColumnOrderHint() {
  override val value: ColumnOrderComparatorFactory
    get() = ColumnOrderComparatorFactory { DefaultColumnComparator }
}
