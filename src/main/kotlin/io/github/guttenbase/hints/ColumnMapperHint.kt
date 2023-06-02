package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnMapper

/**
 * Select target column(s) for given source column. Usually, there will a 1:1 relationship. However, there may be situations where you want
 * to duplicate or transform data into multiple columns.
 *
 *
 * Alternatively the list may also be empty.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 *
 * Hint is used by [io.github.guttenbase.tools.CheckEqualTableDataTool] to map columns
 * Hint is used by [io.github.guttenbase.schema.comparison.SchemaComparatorTool] to map columns
 * Hint is used by [io.github.guttenbase.tools.AbstractTableCopyTool] to map columns
 * Hint is used by [io.github.guttenbase.statements.AbstractStatementCreator] to map columns
 *
 * @author M. Dahm
 */
abstract class  ColumnMapperHint : ConnectorHint<ColumnMapper> {
  override val connectorHintType: Class<ColumnMapper>
    get() = ColumnMapper::class.java
}
