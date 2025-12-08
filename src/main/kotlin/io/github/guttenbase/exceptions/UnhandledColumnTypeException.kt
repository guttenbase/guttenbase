package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when we find a column type we cannot handle (yet).
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class UnhandledColumnTypeException(reason: String) : GuttenBaseException(reason)