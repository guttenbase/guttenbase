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
 * Hint is used by [CheckEqualTableDataTool] to map columns
 * Hint is used by [de.akquinet.jbosscc.guttenbase.tools.schema.comparison.SchemaComparatorTool] to map columns
 * Hint is used by [AbstractTableCopyTool] to map columns
 * Hint is used by [AbstractStatementCreator] to map columns
 *
 * @author M. Dahm
 */
@Suppress("deprecation")
abstract class ColumnMapperHint : ConnectorHint<ColumnMapper> {
  override val connectorHintType: Class<ColumnMapper>
    get() = ColumnMapper::class.java
}
