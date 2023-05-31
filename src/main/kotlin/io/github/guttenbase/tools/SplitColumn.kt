package io.github.guttenbase.tools

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * Sometimes the amount of data exceeds any buffer. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface SplitColumn {
  /**
   * @return column of the table, i.e. in general the primary key or any other column name if no primary key column is appropriate
   */
  fun getSplitColumn(table: TableMetaData): ColumnMetaData
}
