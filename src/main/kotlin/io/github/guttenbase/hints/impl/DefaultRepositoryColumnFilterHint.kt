package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.RepositoryColumnFilterHint
import io.github.guttenbase.repository.RepositoryColumnFilter


/**
 * By default accept any column
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultRepositoryColumnFilterHint : RepositoryColumnFilterHint() {
 override val value: RepositoryColumnFilter
    get() = RepositoryColumnFilter { true }
}
