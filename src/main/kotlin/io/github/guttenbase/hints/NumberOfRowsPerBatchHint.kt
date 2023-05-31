package io.github.guttenbase.hints

import io.github.guttenbase.tools.NumberOfRowsPerBatch

/**
 * How many rows will be inserted in single transaction? This is an important performance issue.
 *
 *
 * We prefer to use use multiple VALUES() clauses for an INSERT statement in order to insert many rows in one batch. This is much faster in
 * most cases than using [PreparedStatement.addBatch]. Unfortunately, not all databases support multiple VALUES() clauses, so the
 * value must be 1 then.
 *
 * The value also must not be too high so that data buffers are not exceeded.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [AbstractTableCopyTool] to determine number of VALUES clauses in INSERT statement or statements in batch update
 *
 * @author M. Dahm
 */
abstract class NumberOfRowsPerBatchHint : ConnectorHint<NumberOfRowsPerBatch> {
  override val connectorHintType: Class<NumberOfRowsPerBatch>
    get() = NumberOfRowsPerBatch::class.java
}
