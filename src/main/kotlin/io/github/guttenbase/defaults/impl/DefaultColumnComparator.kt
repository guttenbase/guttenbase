package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ColumnMetaData


/**
 * By default order by natural order of column names.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnComparator : Comparator<ColumnMetaData> {
  override fun compare(c1: ColumnMetaData, c2: ColumnMetaData) = c1.compareTo(c2)
}
