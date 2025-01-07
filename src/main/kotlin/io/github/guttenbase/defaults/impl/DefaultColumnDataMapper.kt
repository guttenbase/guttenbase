package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapping

/**
 * By default always just return the same object.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) = value
}
