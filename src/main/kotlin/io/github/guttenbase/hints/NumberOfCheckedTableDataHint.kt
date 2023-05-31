package io.github.guttenbase.hints

import io.github.guttenbase.tools.NumberOfCheckedTableData

/**
 * How many rows of the copied tables shall be regarded when checking that data has been transferred correctly with the
 * [CheckEqualTableDataTool] tool.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 * Hint is used by [CheckEqualTableDataTool] How many rows of tables shall be regarded when checking that data has been transferred correctly.
 *
 * @author M. Dahm
 */
abstract class NumberOfCheckedTableDataHint : ConnectorHint<NumberOfCheckedTableData> {
  override val connectorHintType: Class<NumberOfCheckedTableData>
    get() = NumberOfCheckedTableData::class.java
}
