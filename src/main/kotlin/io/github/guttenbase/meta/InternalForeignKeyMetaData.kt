package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface InternalForeignKeyMetaData : ForeignKeyMetaData {
    fun addColumnTuple(referencingColumn: ColumnMetaData, referencedColumn: ColumnMetaData)
}