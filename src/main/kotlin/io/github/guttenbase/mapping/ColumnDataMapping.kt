package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType


/**
 * Container for column data mapping information.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
data class ColumnDataMapping(
  val sourceColumnMetaData: ColumnMetaData,
  val targetColumnMetaData: ColumnMetaData,
  val sourceColumnType: ColumnType,
  val targetColumnType: ColumnType,
  val columnDataMapper: ColumnDataMapper,
  val columnTypeDefinition: ColumnTypeDefinition
)