package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnTypeResolverList

/**
 * Determine strategies to use for mapping different column types. It provides a list of column type resolvers which will be asked in turn
 * to resolve a column type conflict.
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [CommonColumnTypeResolverTool] to determine mapping strategies between different column types
 *
 * @author M. Dahm
 * @see ClassNameColumnTypeResolver
 *
 * @see HeuristicColumnTypeResolver
 */
abstract class ColumnTypeResolverListHint : ConnectorHint<ColumnTypeResolverList> {
  override val connectorHintType: Class<ColumnTypeResolverList>
    get() = ColumnTypeResolverList::class.java
}
