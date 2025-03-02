package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalTableMetaData : TableMetaData {
  override var database: DatabaseMetaData
  override var filteredRowCount: Int
  override var totalRowCount: Int
  override var maxId: Long
  override var minId: Long

  fun addColumn(column: ColumnMetaData)
  fun removeColumn(column: ColumnMetaData)
  fun addIndex(index: IndexMetaData)
  fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun addImportedForeignKey(foreignKey: ForeignKeyMetaData)
  fun addExportedForeignKey(foreignKey: ForeignKeyMetaData)
}
