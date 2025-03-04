package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.Companion.isSupportedColumnType
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.SplitColumn

/**
 * Sometimes the amount of data exceeds buffers. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 * By default use the first primary key column, if any. Otherwise returns the first column with a numeric type. Otherwise return the first
 * column.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultSplitColumn : SplitColumn {
  override fun getSplitColumn(table: TableMetaData): ColumnMetaData {
    val columnMetaData1 = table.columns.firstOrNull { it.isPrimaryKey }
    val columnMetaData2 by lazy {
      table.columns.firstOrNull {
        val columnClassName = it.columnClassName

        if (columnClassName.isSupportedColumnType()) {
          val columnType = ColumnType.valueOfClassName(columnClassName)

          columnType.isNumber
        } else {
          false
        }
      }
    }

    return when {
      columnMetaData1 != null -> columnMetaData1
      columnMetaData2 != null -> columnMetaData2!!
      else -> table.columns[0]
    }
  }
}