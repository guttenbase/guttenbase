package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException


/**
 * Thrown when tables do not match.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class IncompatibleTablesException(reason: String) : GuttenBaseException(reason)
