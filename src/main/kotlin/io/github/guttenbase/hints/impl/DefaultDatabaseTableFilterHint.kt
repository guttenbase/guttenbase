package io.github.guttenbase.hints.impl


import io.github.guttenbase.defaults.impl.DefaultDatabaseTableFilter
import io.github.guttenbase.hints.DatabaseTableFilterHint
import io.github.guttenbase.repository.DatabaseTableFilter

/**
 * Default implementation will accept any table.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultDatabaseTableFilterHint : DatabaseTableFilterHint() {
 override val value: DatabaseTableFilter
    get() = DefaultDatabaseTableFilter()
}
