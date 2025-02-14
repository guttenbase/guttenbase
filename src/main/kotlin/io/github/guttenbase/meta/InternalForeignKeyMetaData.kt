package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalForeignKeyMetaData : ForeignKeyMetaData {
  override var table: TableMetaData

  fun clearReferencingColumns()

  fun clearReferencedColumns()

  fun addColumnTuple(referencingColumn: ColumnMetaData, referencedColumn: ColumnMetaData)
}