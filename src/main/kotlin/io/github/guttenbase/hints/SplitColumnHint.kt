package io.github.guttenbase.hints

import io.github.guttenbase.tools.SplitColumn


/**
 * Sometimes the amount of data exceeds buffers. In these cases we need to split the read data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 *
 * With this hint one may configure the column to be used for splitting.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.statements.SplitByColumnSelectCountStatementCreator]
 * Hint is used by [io.github.guttenbase.statements.SplitByColumnSelectMinMaxStatementCreator]
 * Hint is used by [io.github.guttenbase.statements.SplitByColumnSelectStatementCreator]
 * Hint is used by [io.github.guttenbase.tools.SplitByRangeTableCopyTool]
 *
 * @author M. Dahm
 */
abstract class SplitColumnHint : ConnectorHint<SplitColumn> {
  override val connectorHintType: Class<SplitColumn>
    get() = SplitColumn::class.java
}
