package io.github.guttenbase.hints

import io.github.guttenbase.repository.DatabaseColumnFilter

/**
 * Regard which columns when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns?
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] when reading tables from [io.github.guttenbase.meta.DatabaseMetaData]
 *
 * @author M. Dahm
 */
abstract class DatabaseColumnFilterHint : ConnectorHint<DatabaseColumnFilter> {
  override val connectorHintType: Class<DatabaseColumnFilter>
    get() = DatabaseColumnFilter::class.java
}
