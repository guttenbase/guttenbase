package io.github.guttenbase.hints

import io.github.guttenbase.tools.MaxNumberOfDataItems

/**
 * How many data items may an INSERT statement have. I.e., how many '?' place holders does the database support. This hint may in effect
 * limit the number given by [NumberOfRowsPerBatchHint].
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool] to determine maximum number of data items in INSERT statement
 *
 * @author M. Dahm
 */
abstract class MaxNumberOfDataItemsHint : ConnectorHint<MaxNumberOfDataItems> {
  override val connectorHintType: Class<MaxNumberOfDataItems>
    get() = MaxNumberOfDataItems::class.java
}
