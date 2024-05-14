package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableOrderComparatorFactory

/**
 * By default order by natural order of table names.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("unused")
open class NaturalTableOrderHint : TableOrderHint() {
  override val value: TableOrderComparatorFactory
    get() = TableOrderComparatorFactory { Comparator { o1, o2 -> o1.compareTo(o2) } }
}
