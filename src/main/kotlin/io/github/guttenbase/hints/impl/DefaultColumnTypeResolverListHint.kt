package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ColumnTypeResolverListHint
import io.github.guttenbase.mapping.ColumnTypeResolverList
import io.github.guttenbase.repository.impl.ClassNameColumnTypeResolver
import io.github.guttenbase.repository.impl.HeuristicColumnTypeResolver

/**
 * Default implementation tries [HeuristicColumnTypeResolver] first, then [ClassNameColumnTypeResolver].
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnTypeResolverListHint : ColumnTypeResolverListHint() {
  override val value: ColumnTypeResolverList
    get() = ColumnTypeResolverList { listOf(HeuristicColumnTypeResolver(), ClassNameColumnTypeResolver()) }
}
