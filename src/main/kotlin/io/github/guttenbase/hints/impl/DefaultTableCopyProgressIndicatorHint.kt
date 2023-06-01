package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.TableCopyProgressIndicatorHint
import io.github.guttenbase.utils.LoggingTableCopyProgressIndicator
import io.github.guttenbase.utils.TableCopyProgressIndicator

/**
 * By default return logging implementation.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultTableCopyProgressIndicatorHint : TableCopyProgressIndicatorHint() {
 override val value: TableCopyProgressIndicator
    get() = LoggingTableCopyProgressIndicator()
}
