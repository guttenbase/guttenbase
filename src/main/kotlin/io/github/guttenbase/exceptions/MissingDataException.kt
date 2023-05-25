package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when there is an error while reading the data from source data base.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class MissingDataException(reason: String) : GuttenBaseException(reason) {
  companion object {
    private const val serialVersionUID = 1L
  }
}