package io.github.guttenbase.repository

import io.github.guttenbase.meta.ColumnMetaData

/**
 * This filter is applied when @see [ConnectorRepository.getDatabase] is called.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface RepositoryColumnFilter {
  fun accept(column: ColumnMetaData): Boolean
}
