package io.github.guttenbase.hints

import io.github.guttenbase.repository.DatabaseTableFilter

/**
 * Regard which tables when [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is looking for tables in the given data base. The
 * [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is triggered by default in [io.github.guttenbase.connector.impl.AbstractConnector.retrieveDatabaseMetaData].
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 * Hint is used by [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] when reading tables from [io.github.guttenbase.meta.DatabaseMetaData]
 *
 * @author M. Dahm
 */
abstract class DatabaseTableFilterHint : ConnectorHint<DatabaseTableFilter> {
  override val connectorHintType: Class<DatabaseTableFilter>
    get() = DatabaseTableFilter::class.java
}
