package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException


/**
 * "Fake" [java.sql.SQLException] in order to encapsulate [java.io.IOException] thrown during dumping or restoring data bases using
 * [io.github.guttenbase.export.ImportDumpConnector]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("unused")
class ImportException : GuttenBaseException {
  constructor(reason: String, e: Exception) : super(reason, e)
  constructor(reason: String) : super(reason)

  companion object {
    private const val serialVersionUID = 1L
  }
}