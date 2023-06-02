package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnDataMapperProvider

/**
 * Used to find mappings for column data. E.g., when converting a number to a String or casting a LONG to a BIGINT.
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.tools.CommonColumnTypeResolverTool] to determine mapping between different column types
 *
 * @author M. Dahm
 */
abstract class ColumnDataMapperProviderHint : ConnectorHint<ColumnDataMapperProvider> {
  override val connectorHintType: Class<ColumnDataMapperProvider>
    get() = ColumnDataMapperProvider::class.java
}
