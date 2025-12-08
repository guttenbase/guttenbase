package io.github.guttenbase.meta

import io.github.guttenbase.repository.TableRowCountFilter
import java.sql.JDBCType.BIGINT

/**
 * Information about a table.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
interface TableMetaData : Comparable<TableMetaData>, DatabaseEntityMetaData {
  /**
   * Row count of table using given filter clause [TableRowCountFilter]
   */
  val filteredRowCount: Int

  /**
   * Maximum value of ID (primary key) field if any
   */
  val maxId: Long

  /**
   * Minimum value of ID (primary key) field if any
   */
  val minId: Long

  /**
   * @return containing data base
   */
  fun getIndex(indexName: String): IndexMetaData?
  val primaryKeyColumns: List<ColumnMetaData>
  val indexes: List<IndexMetaData>
  fun getIndexesContainingColumn(column: ColumnMetaData): List<IndexMetaData>
  val importedForeignKeys: List<ForeignKeyMetaData>
  val exportedForeignKeys: List<ForeignKeyMetaData>
}

fun TableMetaData.getNumericPrimaryKeyColumn(): ColumnMetaData? {
  if (primaryKeyColumns.size == 1) {
    val column = primaryKeyColumns[0]

    if (column.jdbcColumnType.isIntegerType() || column.jdbcColumnType.isNumericType() || column.jdbcColumnType == BIGINT) {
      return column
    }
  }

  return null
}
