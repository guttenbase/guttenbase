package io.github.guttenbase.meta

import io.github.guttenbase.repository.TableRowCountFilter
import java.io.Serializable
import java.sql.JDBCType.BIGINT

/**
 * Information about a table.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface TableMetaData : Comparable<TableMetaData>, Serializable {
  /**
   * Row count of table using given filter clause [TableRowCountFilter]
   */
  val filteredRowCount: Int

  /**
   * Total row count of table
   */
  val totalRowCount: Int

  /**
   * Maximum value of ID (primary key) field if any
   */
  val maxId: Long

  /**
   * Minimum value of ID (primary key) field if any
   */
  val minId: Long

  val columnMetaData: List<ColumnMetaData>
  fun getColumnMetaData(columnName: String): ColumnMetaData?
  val columnCount: Int

  /**
   * @return type such as "TABLE" or "VIEW"
   */
  val tableType: String
  val tableName: String
  val tableCatalog: String?
  val tableSchema: String?

  /**
   * @return containing data base
   */
  val databaseMetaData: DatabaseMetaData
  fun getIndexMetaData(indexName: String): IndexMetaData?
  val primaryKeyColumns: List<ColumnMetaData>
  val indexes: List<IndexMetaData>
  fun getIndexesContainingColumn(columnMetaData: ColumnMetaData): List<IndexMetaData>
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

val TableMetaData.databaseType get() = databaseMetaData.databaseType
val TableMetaData.connectorId get() = databaseMetaData.connectorId
val TableMetaData.connectorRepository get() = databaseMetaData.connectorRepository