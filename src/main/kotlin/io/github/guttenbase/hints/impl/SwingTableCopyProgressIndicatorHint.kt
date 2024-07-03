package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.TableCopyProgressIndicatorHint
import io.github.guttenbase.progress.SwingTableCopyProgressIndicator
import io.github.guttenbase.progress.TableCopyProgressIndicator

/**
 * Use UI to show progress.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class SwingTableCopyProgressIndicatorHint : TableCopyProgressIndicatorHint() {
  override val value: TableCopyProgressIndicator
    get() = SwingTableCopyProgressIndicator()
}
