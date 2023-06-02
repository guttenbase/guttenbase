package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableRowDataFilter

/**
 * Filter rows by inspection of data
 *
 *  2022-2034 akquinet tech@spree
 *
 */
abstract class TableRowDataFilterHint : ConnectorHint<TableRowDataFilter> {
  override val connectorHintType: Class<TableRowDataFilter>
    get() = TableRowDataFilter::class.java
}
