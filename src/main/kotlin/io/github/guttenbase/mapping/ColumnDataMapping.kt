package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType


/**
 * Container for column data mapping information.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
data class ColumnDataMapping(
  val sourceColumn: ColumnMetaData,
  val targetColumn: ColumnMetaData,
  val sourceColumnType: ColumnType,
  val targetColumnType: ColumnType,
  val columnDataMapper: ColumnDataMapper,
  val columnTypeDefinition: ColumnTypeDefinition
)