package io.github.guttenbase.hints

import io.github.guttenbase.utils.TableCopyProgressIndicator

/**
 * Select implementation of progress indicator. May be simple logger or fancy UI.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool]
 *
 * @author M. Dahm
 */
abstract class TableCopyProgressIndicatorHint : ConnectorHint<TableCopyProgressIndicator> {
  override val connectorHintType: Class<TableCopyProgressIndicator>
    get() = TableCopyProgressIndicator::class.java
}
