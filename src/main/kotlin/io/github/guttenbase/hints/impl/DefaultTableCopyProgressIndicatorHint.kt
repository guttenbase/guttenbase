package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.TableCopyProgressIndicatorHint
import io.github.guttenbase.progress.TableCopyProgressBarIndicator
import io.github.guttenbase.progress.TableCopyProgressIndicator

/**
 * By default return fancy progress bar implementation.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultTableCopyProgressIndicatorHint : TableCopyProgressIndicatorHint() {
  override val value: TableCopyProgressIndicator
    get() = TableCopyProgressBarIndicator()
}
