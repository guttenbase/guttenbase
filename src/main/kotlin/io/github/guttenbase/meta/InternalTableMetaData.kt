package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalTableMetaData : TableMetaData, InternalDatabaseEntityMetaData {
  override var filteredRowCount: Int
  override var maxId: Long
  override var minId: Long

  fun removeColumn(column: ColumnMetaData)
  fun addIndex(index: IndexMetaData)
  fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun addImportedForeignKey(foreignKey: ForeignKeyMetaData)
  fun addExportedForeignKey(foreignKey: ForeignKeyMetaData)
}
