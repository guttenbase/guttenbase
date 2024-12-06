package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnType


/**
 * Container for column data mapping information.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
data class ColumnDataMapping(
  val sourceColumnType: ColumnType,
  val targetColumnType: ColumnType,
  val columnDataMapper: ColumnDataMapper
)