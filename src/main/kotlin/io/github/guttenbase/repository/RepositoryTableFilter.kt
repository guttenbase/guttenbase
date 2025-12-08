package io.github.guttenbase.repository

import io.github.guttenbase.meta.TableMetaData

/**
 * This filter is applied when @see [ConnectorRepository.getDatabase] is called.
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface RepositoryTableFilter {
  fun accept(table: TableMetaData): Boolean
}
