package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData


/**
 * How many rows will be inserted in single transaction? This is an important performance issue.
 *
 * We support two ways to insert multiple rows in one batch: Either with the [java.sql.PreparedStatement.addBatch] method or with multiple
 * VALUES() clauses for an INSERT statement. The latter method is much faster in most cases, but not all databases support this, so the
 * value must be 1 then.
 *
 * The value also must not be too high so data buffers are not exceeded, especially when the table contains BLOBs.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface BatchInsertionConfiguration {
  fun getNumberOfRowsPerBatch(targetTable: TableMetaData): Int

  /**
   * How many data items may the INSERT statement have in total. I.e., how many '?' placeholders does the database support in a single
   * statement.
   */
  fun getMaxNumberOfDataItems(targetTable: TableMetaData): Int

  /**
   * Use VALUES() clauses or [java.sql.PreparedStatement.addBatch] as discussed above
   */
  fun useMultipleValuesClauses(targetTable: TableMetaData) = true
}
