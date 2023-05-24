package io.github.guttenbase.repository

import io.github.guttenbase.meta.TableMetaData

/**
 * This filter is applied when @see [ConnectorRepository.getDatabaseMetaData] is called.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface RepositoryTableFilter {
    fun accept(table: TableMetaData): Boolean
}
