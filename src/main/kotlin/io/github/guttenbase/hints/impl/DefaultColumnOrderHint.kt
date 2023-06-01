package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultColumnComparator
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.mapping.ColumnOrderComparatorFactory

/**
 * By default order by natural order of column names.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultColumnOrderHint : ColumnOrderHint() {
  override val value: ColumnOrderComparatorFactory
    get() = ColumnOrderComparatorFactory { DefaultColumnComparator() }
}
