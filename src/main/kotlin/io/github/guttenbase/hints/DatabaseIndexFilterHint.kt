package io.github.guttenbase.hints

import io.github.guttenbase.repository.DatabaseIndexFilter

/**
 * Filter indexes when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] when reading tables from [io.github.guttenbase.meta.DatabaseMetaData]
 *
 * @author M. Dahm
 */
abstract class DatabaseIndexFilterHint : ConnectorHint<DatabaseIndexFilter> {
  override val connectorHintType: Class<DatabaseIndexFilter>
    get() = DatabaseIndexFilter::class.java
}
