package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ColumnMetaData


/**
 * By default order by natural order of column names, but put primary key first if any
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnComparator : Comparator<ColumnMetaData> {
  override fun compare(c1: ColumnMetaData, c2: ColumnMetaData) = when {
    c1.isPrimaryKey && !c2.isPrimaryKey -> -1
    !c1.isPrimaryKey && c2.isPrimaryKey -> 1
    else -> c1.compareTo(c2)
  }
}
