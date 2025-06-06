package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultDatabaseColumnFilter
import io.github.guttenbase.hints.DatabaseColumnFilterHint
import io.github.guttenbase.repository.DatabaseColumnFilter

/**
 * Default implementation will accept any column.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
object DefaultDatabaseColumnFilterHint : DatabaseColumnFilterHint() {
  override val value: DatabaseColumnFilter
    get() = DefaultDatabaseColumnFilter()
}
