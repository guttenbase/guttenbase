package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.*
import java.util.*

/**
 * Information about a table.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TableMetaDataImpl(
  override val tableName: String,
  override val databaseMetaData: DatabaseMetaData,
  override val tableType: String,
  override val tableCatalog: String,
  override val tableSchema: String
) : InternalTableMetaData {

  /**
   * {@inheritDoc}
   */
  override var totalRowCount = 0

  /**
   * {@inheritDoc}
   */
  override var filteredRowCount = 0

  override val columnCount: Int
    get() = columnMetaData.size

  private val columnMap = LinkedHashMap<String, ColumnMetaData>()
  private val indexMap = LinkedHashMap<String, IndexMetaData>()
  private val importedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()
  private val exportedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()

  // Derived values
  override val columnMetaData: List<ColumnMetaData> get() = ArrayList(columnMap.values)

  override val indexes: List<IndexMetaData> get() = ArrayList(indexMap.values)

  override val exportedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(exportedForeignKeyMap.values)

  override val importedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(importedForeignKeyMap.values)

  override val primaryKeyColumns: List<ColumnMetaData>
    get() = columnMetaData.filter(ColumnMetaData::isPrimaryKey)

  /**
   * {@inheritDoc}
   */
  override fun getColumnMetaData(columnName: String): ColumnMetaData? = columnMap[columnName.uppercase(Locale.getDefault())]

  /**
   * {@inheritDoc}
   */
  override fun addColumn(column: ColumnMetaData) {
    columnMap[column.columnName.uppercase()] = column
  }

  /**
   * {@inheritDoc}
   */
  override fun removeColumn(columnMetaData: ColumnMetaData) {
    columnMap.remove(columnMetaData.columnName.uppercase())
  }

  override fun getIndexMetaData(indexName: String): IndexMetaData? {
    return indexMap[indexName.uppercase()]
  }

  override fun addIndex(indexMetaData: IndexMetaData) {
    indexMap[indexMetaData.indexName.uppercase()] = indexMetaData
  }

  override fun addExportedForeignKey(fkMetaData: ForeignKeyMetaData) {
    exportedForeignKeyMap[fkMetaData.foreignKeyName.uppercase()] = fkMetaData
  }

  override fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData? =
    exportedForeignKeyMap[foreignKeyname.uppercase()]

  override fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData? =
    importedForeignKeyMap[foreignKeyname.uppercase()]

  override fun addImportedForeignKey(fkMetaData: ForeignKeyMetaData) {
    importedForeignKeyMap[fkMetaData.foreignKeyName.uppercase()] = fkMetaData
  }

  override fun getIndexesContainingColumn(columnMetaData: ColumnMetaData): List<IndexMetaData> =
    indexes.map { it.columnMetaData.filter { c -> c == columnMetaData }.map { _ -> it } }.flatten()

  override operator fun compareTo(other: TableMetaData) =
    tableName.uppercase(Locale.getDefault()).compareTo(other.tableName.uppercase())

  override fun toString() = tableName

  override fun hashCode() = tableName.uppercase().hashCode()

  override fun equals(other: Any?): Boolean {
    val that: TableMetaData = other as TableMetaData
    return tableName.equals(that.tableName, ignoreCase = true)
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
