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
interface InternalDatabaseMetaData : DatabaseMetaData {
    fun addTableMetaData(tableMetaData: TableMetaData)
    fun removeTableMetaData(tableMetaData: TableMetaData)
}