package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when tables have mismatching columns.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * @see InsertStatementFiller
 *
 * @see SchemaComparatorTool
 *
 * @see CheckEqualTableDataTool
 */
class IncompatibleColumnsException(reason: String) : GuttenBaseException(reason) {
    companion object {
        private const val serialVersionUID = 1L
    }
}
