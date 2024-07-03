package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.TableCopyProgressIndicatorHint
import io.github.guttenbase.progress.LoggingTableCopyProgressIndicator
import io.github.guttenbase.progress.TableCopyProgressIndicator

/**
 * By default return logging implementation.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultTableCopyProgressIndicatorHint : TableCopyProgressIndicatorHint() {
 override val value: TableCopyProgressIndicator
    get() = LoggingTableCopyProgressIndicator()
}
