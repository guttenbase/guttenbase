package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalIndexMetaData : IndexMetaData {
  override var table: TableMetaData

  fun addColumn(column: ColumnMetaData)

  fun clearColumns()
}
