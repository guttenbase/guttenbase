package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.RepositoryTableFilterHint
import io.github.guttenbase.repository.RepositoryTableFilter


/**
 * Default implementation will accept any table.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultRepositoryTableFilterHint : RepositoryTableFilterHint() {
 override val value: RepositoryTableFilter
    get() = RepositoryTableFilter {  true }
}
