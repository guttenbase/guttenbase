package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper

/**
 * Map tables between source and target
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [CheckEqualTableDataTool] to get according table
 * Hint is used by [SchemaComparatorTool] to get according table
 * Hint is used by [AbstractTableCopyTool] to get according table
 *
 * @author M. Dahm
 */
@Suppress("deprecation")
abstract class TableMapperHint : ConnectorHint<TableMapper> {
  override val connectorHintType: Class<TableMapper>
    get() = TableMapper::class.java
}
