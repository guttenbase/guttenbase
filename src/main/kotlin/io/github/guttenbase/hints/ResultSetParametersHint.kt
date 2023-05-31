package io.github.guttenbase.hints

import io.github.guttenbase.tools.ResultSetParameters


/**
 *
 * Set fetch size, result set type and concurrency tye for result set,
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 *
 *
 * Hint is used by [AbstractTableCopyTool] to determine number of VALUES clauses in INSERT statement or statements in batch update
 */
abstract class ResultSetParametersHint : ConnectorHint<ResultSetParameters> {
  override val connectorHintType: Class<ResultSetParameters>
    get() = ResultSetParameters::class.java
}
