package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnDataMapperProvider

/**
 * Used to find mappings for column data. E.g., when converting a number to a String or casting a LONG to a BIGINT.
 *
 * &copy; 2012-2044 tech@spree
 *
 * Hint is used by [io.github.guttenbase.tools.ColumnDataMappingTool] to determine mapping between different column types
 *
 * @author M. Dahm
 */
abstract class ColumnDataMapperProviderHint : ConnectorHint<ColumnDataMapperProvider> {
  override val connectorHintType: Class<ColumnDataMapperProvider>
    get() = ColumnDataMapperProvider::class.java
}
