package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryColumnFilter


/**
 * This filter is applied when @see [io.github.guttenbase.repository.ConnectorRepository.getDatabaseMetaData] is called.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.repository.ConnectorRepository.getDatabaseMetaData] when returning table meta data and their respective columns
 *
 * @author M. Dahm
 */
abstract class RepositoryColumnFilterHint : ConnectorHint<RepositoryColumnFilter> {
  override val connectorHintType: Class<RepositoryColumnFilter>
    get() = RepositoryColumnFilter::class.java
}
