package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Map column types to Java types. Every data base has its own special types which have to be mapped to standard types somehow.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface ColumnTypeResolver {
  /**
   * Tries to map column to a known type.
   */
  fun getColumnType(columnMetaData: ColumnMetaData): ColumnType?
}