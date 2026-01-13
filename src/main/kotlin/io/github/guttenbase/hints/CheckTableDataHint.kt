package io.github.guttenbase.hints

import io.github.guttenbase.tools.CheckTableData

/**
 * How many rows of the copied tables shall be regarded when checking that data has been transferred correctly with the
 * [io.github.guttenbase.tools.CheckEqualTableDataTool] tool.
 *
 * &copy; 2012-2044 tech@spree
 *
 * Hint is used by [io.github.guttenbase.tools.CheckEqualTableDataTool] How many rows of tables shall be regarded when checking that data has been transferred correctly.
 *
 * @author M. Dahm
 */
abstract class CheckTableDataHint : ConnectorHint<CheckTableData> {
  override val connectorHintType: Class<CheckTableData>
    get() = CheckTableData::class.java
}
