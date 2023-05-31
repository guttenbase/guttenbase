package io.github.guttenbase.hints

import io.github.guttenbase.repository.RepositoryColumnFilter


/**
 * This filter is applied when @see [ConnectorRepository.getDatabaseMetaData] is called.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [ConnectorRepository.getDatabaseMetaData] when returning table meta data and their respective columns
 *
 * @author M. Dahm
 */
abstract class RepositoryColumnFilterHint : ConnectorHint<RepositoryColumnFilter> {
  override val connectorHintType: Class<RepositoryColumnFilter>
    get() = RepositoryColumnFilter::class.java
}
