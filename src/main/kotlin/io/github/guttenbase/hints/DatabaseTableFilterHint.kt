package io.github.guttenbase.hints

import io.github.guttenbase.repository.DatabaseTableFilter

/**
 * Regard which tables when [DatabaseMetaDataInspectorTool] is looking for tables in the given data base. The
 * [DatabaseMetaDataInspectorTool] is triggered by default in [AbstractConnector.retrieveDatabaseMetaData].
 *
 *
 *  2012-2034 akquinet tech@spree
 * Hint is used by [DatabaseMetaDataInspectorTool] when reading tables from [DatabaseMetaData]
 *
 * @author M. Dahm
 */
abstract class DatabaseTableFilterHint : ConnectorHint<DatabaseTableFilter> {
  override val connectorHintType: Class<DatabaseTableFilter>
    get() = DatabaseTableFilter::class.java
}
