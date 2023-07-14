package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when we find a column type we cannot handle (yet).
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class UnhandledColumnTypeException(reason: String) : GuttenBaseException(reason) {
  companion object {
    private const val serialVersionUID = 1L
  }
}