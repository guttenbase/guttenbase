package io.github.guttenbase.hints

import io.github.guttenbase.repository.DatabaseColumnFilter

/**
 * Regard which columns when @see [DatabaseMetaDataInspectorTool] is inquiring the database for columns?
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [DatabaseMetaDataInspectorTool] when reading tables from [DatabaseMetaData]
 *
 * @author M. Dahm
 */
abstract class DatabaseColumnFilterHint : ConnectorHint<DatabaseColumnFilter> {
  override val connectorHintType: Class<DatabaseColumnFilter>
    get() = DatabaseColumnFilter::class.java
}
