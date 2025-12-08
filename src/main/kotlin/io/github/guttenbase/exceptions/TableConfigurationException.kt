package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when data bases have not the same tables. You can omit tables deliberately using the [io.github.guttenbase.repository.RepositoryTableFilter] hint.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class TableConfigurationException(reason: String) : GuttenBaseException(reason)