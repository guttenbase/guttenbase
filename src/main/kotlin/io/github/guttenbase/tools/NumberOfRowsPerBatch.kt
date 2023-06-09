package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData


/**
 * How many rows will be inserted in single transaction? This is an important performance issue.
 *
 * We support two ways to insert multiple rows in one batch: Either with the [java.sql.PreparedStatement.addBatch] method or with multiple
 * VALUES() clauses for an INSERT statement. The latter method is much faster in most cases, but not all databases support this, so the
 * value must be 1 then.
 *
 *
 * The value also must not be too high so data buffers are not exceeded, especially when the table contains BLOBs.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * @see MaxNumberOfDataItems
 */
fun interface NumberOfRowsPerBatch {
  fun getNumberOfRowsPerBatch(targetTableMetaData: TableMetaData): Int

  /**
   * Use VALUES() clauses or [java.sql.PreparedStatement.addBatch] as discussed above
   */
  fun useMultipleValuesClauses(targetTableMetaData: TableMetaData) = true
}
