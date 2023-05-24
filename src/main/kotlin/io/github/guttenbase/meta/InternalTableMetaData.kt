package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface InternalTableMetaData : TableMetaData {
    fun setTotalRowCount(rowCount: Int)
    fun setFilteredRowCount(rowCount: Int)
    fun addColumn(column: ColumnMetaData)
    fun removeColumn(columnMetaData: ColumnMetaData)
    fun addIndex(indexMetaData: IndexMetaData)
    fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData
    fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData
    fun addImportedForeignKey(fkMetaData: ForeignKeyMetaData)
    fun addExportedForeignKey(fkMetaData: ForeignKeyMetaData)
}
