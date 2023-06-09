package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when table data is checked for equality.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * @see io.github.guttenbase.tools.CheckEqualTableDataTool
 */
class UnequalNumberOfRowsException(reason: String) : GuttenBaseException(reason) {
  companion object {
    private const val serialVersionUID = 1L
  }
}