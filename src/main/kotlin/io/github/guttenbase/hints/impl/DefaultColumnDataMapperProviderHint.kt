package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.hints.ColumnDataMapperProviderHint
import io.github.guttenbase.mapping.ColumnDataMapperProvider

/**
 * You may add additional mappings by using [DefaultColumnDataMapperProvider.addMapping].
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnDataMapperProviderHint : ColumnDataMapperProviderHint() {
  override val value: ColumnDataMapperProvider
    get() = DefaultColumnDataMapperProvider
}
