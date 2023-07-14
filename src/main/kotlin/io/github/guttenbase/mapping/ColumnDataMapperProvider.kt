package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Used to find mappings for column data. E.g., when converting a number to a String or casting a LONG to a BIGINT.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ColumnDataMapperProvider {
  /**
   * Find mapping the given configuration.
   *
   * @param sourceColumnMetaData source column
   * @param targetColumnMetaData target column
   * @param sourceColumnType     as determined by [ColumnTypeResolver]
   * @param targetColumnType     as determined by [ColumnTypeResolver]
   */
  fun findMapping(
    sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData,
    sourceColumnType: ColumnType, targetColumnType: ColumnType
  ): ColumnDataMapper?

  /**
   * Specify additional mapping
   */
  fun addMapping(sourceColumnType: ColumnType, targetColumnType: ColumnType, columnDataMapper: ColumnDataMapper)
}
