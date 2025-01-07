package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData


/**
 * Create (initial) column type definition for given column and database type.
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
fun interface ColumnTypeDefinitionFactory {
  fun createColumnDefinition(sourceColumn: ColumnMetaData, targetDatabase: DatabaseMetaData): ColumnTypeDefinition?
}
