package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException


/**
 * "Fake" [SQLException] in order to encapsulate [IOException] thrown during dumping or restoring data bases using
 * [ExportDumpConnector]
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportException : GuttenBaseException {
    constructor(reason: String, e: Exception) : super(reason, e)
    constructor(reason: String) : super(reason)

    companion object {
        private const val serialVersionUID = 1L
    }
}