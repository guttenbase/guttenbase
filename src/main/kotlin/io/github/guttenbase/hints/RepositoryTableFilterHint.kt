package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryTableFilter


/**
 * This filter is applied when @see [io.github.guttenbase.repository.ConnectorRepository.getDatabase] is called.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.repository.ConnectorRepository.getDatabase] when returning table meta data
 *
 * @author M. Dahm
 */
abstract class RepositoryTableFilterHint : ConnectorHint<RepositoryTableFilter> {
  override val connectorHintType: Class<RepositoryTableFilter>
    get() = RepositoryTableFilter::class.java
}
