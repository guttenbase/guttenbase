package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper

/**
 * Map tables between source and target
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.tools.CheckEqualTableDataTool] to get according table
 * Hint is used by [io.github.guttenbase.schema.comparison.SchemaComparatorTool] to get according table
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool] to get according table
 *
 * @author M. Dahm
 */
abstract class TableMapperHint : ConnectorHint<TableMapper> {
  override val connectorHintType: Class<TableMapper>
    get() = TableMapper::class.java
}
