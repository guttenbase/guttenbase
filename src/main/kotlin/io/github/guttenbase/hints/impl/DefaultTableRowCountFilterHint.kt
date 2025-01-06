package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultTableRowCountFilter
import io.github.guttenbase.hints.TableRowCountFilterHint
import io.github.guttenbase.repository.TableRowCountFilter


/**
 * By default compute row count for all tables
 */
object DefaultTableRowCountFilterHint : TableRowCountFilterHint() {
 override val value: TableRowCountFilter
    get() = DefaultTableRowCountFilter()
}
