package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
internal interface InternalDatabaseMetaData : DatabaseMetaData {
  fun addTable(tableMetaData: TableMetaData)
  fun removeTable(tableMetaData: TableMetaData)
}