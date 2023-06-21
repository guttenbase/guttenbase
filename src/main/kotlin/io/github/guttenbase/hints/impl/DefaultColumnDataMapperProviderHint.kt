package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.hints.ColumnDataMapperProviderHint
import io.github.guttenbase.mapping.BigDecimalToLongColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapperProvider
import io.github.guttenbase.mapping.LongToBigDecimalColumnDataMapper
import io.github.guttenbase.mapping.TimestampToDateColumnDataMapper
import io.github.guttenbase.meta.ColumnType

/**
 * Default implementation. You may inherit from this class and override [.addMappings] to
 * add further mappings.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnDataMapperProviderHint : ColumnDataMapperProviderHint() {
  override val value: ColumnDataMapperProvider
    get() {
      val result = DefaultColumnDataMapperProvider()
      addMappings(result)
      return result
    }

  /**
   * May be overridden to add further mappings
   */
  protected open fun addMappings(columnDataMapperFactory: DefaultColumnDataMapperProvider) {
    columnDataMapperFactory.addMapping(ColumnType.CLASS_TIMESTAMP, ColumnType.CLASS_DATE, TimestampToDateColumnDataMapper())
    columnDataMapperFactory.addMapping(
      ColumnType.CLASS_LONG, ColumnType.CLASS_BIGDECIMAL,
      LongToBigDecimalColumnDataMapper()
    )
    columnDataMapperFactory
      .addMapping(ColumnType.CLASS_BIGDECIMAL, ColumnType.CLASS_LONG, BigDecimalToLongColumnDataMapper())
    columnDataMapperFactory.addMapping(
      ColumnType.CLASS_INTEGER, ColumnType.CLASS_BIGDECIMAL,
      LongToBigDecimalColumnDataMapper()
    )
  }
}
