package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.SelectWhereClauseHint
import io.github.guttenbase.tools.SelectWhereClause


/**
 * Default is no WHERE clause.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
object DefaultSelectWhereClauseHint : SelectWhereClauseHint() {
 override val value: SelectWhereClause
    get() = SelectWhereClause { "" }
}
