package io.github.guttenbase.hints

import io.github.guttenbase.tools.RefreshTargetConnection

/**
 * Some JDBC drivers seem to accumulate data over time, even after a connection is commited() and all statements, result sets, etc. are closed.
 * This will cause an OutOfMemoryError eventually.<br></br>
 * To avoid this the connection can be flushed, closed and re-established periodically using this hint.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 *
 *
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool] to determine table order
 */
abstract class RefreshTargetConnectionHint : ConnectorHint<RefreshTargetConnection> {
  override val connectorHintType: Class<RefreshTargetConnection>
    get() = RefreshTargetConnection::class.java
}
