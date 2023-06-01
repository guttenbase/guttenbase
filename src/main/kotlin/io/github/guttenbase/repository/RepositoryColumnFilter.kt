package io.github.guttenbase.repository

import io.github.guttenbase.meta.ColumnMetaData

/**
 * This filter is applied when @see [ConnectorRepository.getDatabaseMetaData] is called.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface RepositoryColumnFilter {
  fun accept(column: ColumnMetaData): Boolean
}
