package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.RepositoryColumnFilterHint
import io.github.guttenbase.repository.RepositoryColumnFilter


/**
 * By default accept any column
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultRepositoryColumnFilterHint : RepositoryColumnFilterHint() {
 override val value: RepositoryColumnFilter
    get() = RepositoryColumnFilter { true }
}
