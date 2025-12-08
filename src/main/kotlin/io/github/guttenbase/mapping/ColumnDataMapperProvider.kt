package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Used to find mappings for column data. E.g., when converting a number to a String or casting a LONG to a BIGINT.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
interface ColumnDataMapperProvider {
  /**
   * Find mapping the given configuration.
   *
   * @param sourceColumnMetaData source column
   * @param targetColumnMetaData target column
   * @param sourceColumnType     source column type
   * @param targetColumnType     target column type
   */
  fun findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
    sourceColumnType: ColumnType, targetColumnType: ColumnType
  ): ColumnDataMapper?
}
