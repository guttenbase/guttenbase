package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
internal interface InternalForeignKeyMetaData : ForeignKeyMetaData {
  fun addColumnTuple(referencingColumn: ColumnMetaData, referencedColumn: ColumnMetaData)
}