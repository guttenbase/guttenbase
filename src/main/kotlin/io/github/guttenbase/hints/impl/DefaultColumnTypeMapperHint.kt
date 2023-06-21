package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ColumnTypeMapperHint
import io.github.guttenbase.mapping.ColumnTypeMapper
import io.github.guttenbase.mapping.DefaultColumnTypeMapper

/**
 * By default use customized mapping since database column types are sometimes different.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnTypeMapperHint : ColumnTypeMapperHint() {
  override val value: ColumnTypeMapper
    get() = DefaultColumnTypeMapper()
}
