package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryColumnFilter


/**
 * This filter is applied when @see [io.github.guttenbase.repository.ConnectorRepository.getDatabase] is called.
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.repository.ConnectorRepository.getDatabase] when returning table meta data and their respective columns
 *
 * @author M. Dahm
 */
abstract class RepositoryColumnFilterHint : ConnectorHint<RepositoryColumnFilter> {
  override val connectorHintType: Class<RepositoryColumnFilter>
    get() = RepositoryColumnFilter::class.java
}
