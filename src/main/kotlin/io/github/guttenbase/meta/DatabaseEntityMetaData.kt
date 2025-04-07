package io.github.guttenbase.meta

/**
 * Common data of views and tables.
 *
 * &copy; 2025-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface DatabaseEntityMetaData : MetaData {
  /**
   * Total row count of table
   */
  val totalRowCount: Int

  val columns: List<ColumnMetaData>
  fun getColumn(columnName: String): ColumnMetaData?
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
  val database: DatabaseMetaData
}

val DatabaseEntityMetaData.databaseType get() = database.databaseType
val DatabaseEntityMetaData.connectorId get() = database.connectorId
val DatabaseEntityMetaData.connectorRepository get() = database.connectorRepository

fun DatabaseEntityMetaData.isView() = this is ViewMetaData
fun DatabaseEntityMetaData.isTable() = this is TableMetaData