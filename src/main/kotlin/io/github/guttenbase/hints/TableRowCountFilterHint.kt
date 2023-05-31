package io.github.guttenbase.hints

import io.github.guttenbase.repository.TableRowCountFilter


/**
 * Some tables are really big and computing the row count may take too long for the data base.
 *
 *
 * Using this hint the @see [DatabaseMetaDataInspectorTool] will compute the row count only
 * for the given tables.
 */
abstract class TableRowCountFilterHint : ConnectorHint<TableRowCountFilter> {
  override val connectorHintType: Class<TableRowCountFilter>
    get() = TableRowCountFilter::class.java
}
