package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ColumnTypeMapperHint
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.DefaultColumnTypeMapper

/**
 * By default use customizable mapping.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnTypeMapperHint : ColumnTypeMapperHint() {
  override val value: ColumnTypeMapper get() = DefaultColumnTypeMapper
}
