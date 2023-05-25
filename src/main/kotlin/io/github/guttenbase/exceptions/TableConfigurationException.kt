package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when data bases have not the same tables. You can omit tables deliberately using the [RepositoryTableFilter] hint.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class TableConfigurationException(reason: String) : GuttenBaseException(reason) {
  companion object {
    private const val serialVersionUID = 1L
  }
}