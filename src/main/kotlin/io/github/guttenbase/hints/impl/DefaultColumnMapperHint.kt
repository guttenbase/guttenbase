package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ColumnMapperHint
import io.github.guttenbase.mapping.ColumnMapper

/**
 * By default return column with same name ignoring case.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnMapperHint(private val columnMapper: ColumnMapper) : ColumnMapperHint() {
  override val value: ColumnMapper
    get() = columnMapper
}
