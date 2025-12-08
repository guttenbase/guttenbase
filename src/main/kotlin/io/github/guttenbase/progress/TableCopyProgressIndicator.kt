package io.github.guttenbase.progress

/**
 * Show progress when copying tables. Simple implementation will just log to console.
 *
 * &copy; 2013-2034 tech@spree
 *
 *
 * @author M. Dahm
 */
interface TableCopyProgressIndicator : ProgressIndicator {
  fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String)
}
