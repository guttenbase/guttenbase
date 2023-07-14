package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalTableMetaData : TableMetaData {
  override var filteredRowCount: Int
  override var totalRowCount: Int

  fun addColumn(column: ColumnMetaData)
  fun removeColumn(columnMetaData: ColumnMetaData)
  fun addIndex(indexMetaData: IndexMetaData)
  fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData?
  fun addImportedForeignKey(fkMetaData: ForeignKeyMetaData)
  fun addExportedForeignKey(fkMetaData: ForeignKeyMetaData)
}
