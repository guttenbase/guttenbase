package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData


/**
 * Some JDBC drivers seem to accumulate data over time, even after a connection is commited() and all statements, result sets, etc. are closed.
 * This will cause an OutOfMemoryError eventually.<br></br>
 * To avoid this the connection can be flushed, closed and re-established periodically using this hint.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface RefreshTargetConnection {
  fun refreshConnection(noCopiedTables: Int, sourceTableMetaData: TableMetaData): Boolean = false
}
