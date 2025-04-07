package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.Locale

internal val REPO_FOR_SERIALIZATION = ConnectorRepository()
internal val DB_FOR_SERIALIZATION = DatabaseMetaDataImpl(REPO_FOR_SERIALIZATION, "", "", mapOf(), DatabaseType.GENERIC)
internal val TABLE_FOR_SERIALIZATION = TableMetaDataImpl(DB_FOR_SERIALIZATION, "", "", null, null)

/**
 * Information about a table.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Serializable
class TableMetaDataImpl(
  @Transient
  override var database: DatabaseMetaData = DB_FOR_SERIALIZATION,
  override val tableName: String,
  override val tableType: String,
  override val tableCatalog: String?,
  override val tableSchema: String?
) : InternalTableMetaData, DatabaseEntityMetaDataImpl() {
  constructor(database: DatabaseMetaData, table: TableMetaData) : this(
    database, table.tableName, table.tableType, table.tableCatalog, table.tableSchema
  )

  /**
   * {@inheritDoc}
   */
  override var maxId = 0L

  /**
   * {@inheritDoc}
   */
  override var minId = 0L

  /**
   * {@inheritDoc}
   */
  override var filteredRowCount = 0

  private val indexMap = LinkedHashMap<String, IndexMetaData>()
  private val importedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()
  private val exportedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()

  //
  // Derived values
  //
  override val indexes: List<IndexMetaData> get() = ArrayList(indexMap.values)

  override val exportedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(exportedForeignKeyMap.values)

  override val importedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(importedForeignKeyMap.values)

  override val primaryKeyColumns: List<ColumnMetaData>
    get() = columns.filter(ColumnMetaData::isPrimaryKey)

  /**
   * {@inheritDoc}
   */
  override fun addColumn(column: ColumnMetaData) {
    columnMap[column.columnName.uppercase()] = column
  }

  /**
   * {@inheritDoc}
   */
  override fun removeColumn(column: ColumnMetaData) {
    columnMap.remove(column.columnName.uppercase())
  }

  override fun getIndex(indexName: String): IndexMetaData? {
    return indexMap[indexName.uppercase()]
  }

  override fun addIndex(index: IndexMetaData) {
    indexMap[index.indexName.uppercase()] = index
  }

  override fun addExportedForeignKey(foreignKey: ForeignKeyMetaData) {
    exportedForeignKeyMap[foreignKey.foreignKeyName.uppercase()] = foreignKey
  }

  override fun getExportedForeignKey(foreignKeyname: String): ForeignKeyMetaData? =
    exportedForeignKeyMap[foreignKeyname.uppercase()]

  override fun getImportedForeignKey(foreignKeyname: String): ForeignKeyMetaData? =
    importedForeignKeyMap[foreignKeyname.uppercase()]

  override fun addImportedForeignKey(foreignKey: ForeignKeyMetaData) {
    importedForeignKeyMap[foreignKey.foreignKeyName.uppercase()] = foreignKey
  }

  override fun getIndexesContainingColumn(column: ColumnMetaData): List<IndexMetaData> =
    indexes.map { it.columns.filter { c -> c == column }.map { _ -> it } }.flatten()

  override operator fun compareTo(other: TableMetaData) =
    tableName.uppercase(Locale.getDefault()).compareTo(other.tableName.uppercase())
}
