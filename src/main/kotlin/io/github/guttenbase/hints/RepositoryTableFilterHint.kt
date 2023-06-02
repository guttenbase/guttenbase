package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryTableFilter


/**
 * This filter is applied when @see [io.github.guttenbase.repository.ConnectorRepository.getDatabaseMetaData] is called.
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.repository.ConnectorRepository.getDatabaseMetaData] when returning table meta data
 *
 * @author M. Dahm
 */
abstract class RepositoryTableFilterHint : ConnectorHint<RepositoryTableFilter> {
  override val connectorHintType: Class<RepositoryTableFilter>
    get() = RepositoryTableFilter::class.java
}
