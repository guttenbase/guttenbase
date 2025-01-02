package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.tools.ColumnMapping

/**
 * By default always just return the same object.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnMapping, value: Any?) = value
}
