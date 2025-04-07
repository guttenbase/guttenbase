package io.github.guttenbase.exceptions

import io.github.guttenbase.connector.GuttenBaseException

/**
 * Thrown when tables have mismatching columns.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 * @see io.github.guttenbase.statements.InsertStatementFiller
 *
 * @see io.github.guttenbase.schema.comparison.SchemaComparatorTool
 *
 * @see io.github.guttenbase.tools.CheckEqualTableDataTool
 */
class IncompatibleColumnsException(reason: String) : GuttenBaseException(reason)
