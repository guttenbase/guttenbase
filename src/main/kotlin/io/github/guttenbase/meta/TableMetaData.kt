package io.github.guttenbase.meta

import java.io.Serializable

/**
 * Information about a table.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface TableMetaData : Comparable<TableMetaData>, Serializable {
    val filteredRowCount: Int
    val totalRowCount: Int
    val columnMetaData: List<ColumnMetaData>
    fun getColumnMetaData(columnName: String): ColumnMetaData
    val columnCount: Int

    /**
     * @return type such as "TABLE" or "VIEW"
     */
    val tableType: String
    val tableName: String

    /**
     * @return containing data base
     */
    val databaseMetaData: DatabaseMetaData
    fun getIndexMetaData(indexName: String): IndexMetaData
    val primaryKeyColumns: List<ColumnMetaData>
    val indexes: List<IndexMetaData>
    fun getIndexesContainingColumn(columnMetaData: ColumnMetaData): List<IndexMetaData>
    val importedForeignKeys: List<ForeignKeyMetaData>
    val exportedForeignKeys: List<ForeignKeyMetaData>
}
