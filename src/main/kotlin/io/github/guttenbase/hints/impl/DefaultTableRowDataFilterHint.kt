package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.TableRowDataFilterHint
import io.github.guttenbase.mapping.TableRowDataFilter

/**
 * By default, accept all data
 */
class DefaultTableRowDataFilterHint : TableRowDataFilterHint() {
  override val value: TableRowDataFilter
    get() = TableRowDataFilter { _, _ -> true }
}
