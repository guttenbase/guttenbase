package io.github.guttenbase.utils

/**
 * Show progress when copying tables. Simple implementation will just log to console.
 *
 *  2013-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface TableCopyProgressIndicator : ProgressIndicator {
  fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String)
}
