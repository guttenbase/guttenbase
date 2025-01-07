package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.TableCopyProgressIndicatorHint
import io.github.guttenbase.progress.SwingTableCopyProgressIndicator
import io.github.guttenbase.progress.TableCopyProgressIndicator

/**
 * Use UI to show progress.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object SwingTableCopyProgressIndicatorHint : TableCopyProgressIndicatorHint() {
  override val value: TableCopyProgressIndicator
    get() = SwingTableCopyProgressIndicator()
}
