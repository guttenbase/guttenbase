package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *
 *  2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface InternalIndexMetaData : IndexMetaData {
  fun addColumn(columnMetaData: ColumnMetaData)
}
