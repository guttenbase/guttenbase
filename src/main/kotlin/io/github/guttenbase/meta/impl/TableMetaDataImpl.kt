package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.*

internal val REPO_FOR_SERIALIZATION = ConnectorRepository()
internal val DB_FOR_SERIALIZATION = DatabaseMetaDataImpl(REPO_FOR_SERIALIZATION, "", "", mapOf(), DatabaseType.GENERIC)
internal val TABLE_FOR_SERIALIZATION = TableMetaDataImpl(DB_FOR_SERIALIZATION, "", "", null, null)
//val DUMMYFK = ForeignKeyMetaDataImpl(DUMMYTABLE, "", mutableListOf(), mutableListOf())
//val DUMMYCOLUMN = ColumnMetaDataImpl(DUMMYTABLE, 0, "", "", "", false, false, 0, 0)

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
) : InternalTableMetaData {
  constructor(database: DatabaseMetaData, table: TableMetaData) : this(
    database, table.tableName, table.tableType, table.tableCatalog, table.tableSchema
  )

  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val syntheticId = UUID.randomUUID()!!

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
  override var totalRowCount = 0

  /**
   * {@inheritDoc}
   */
  override var filteredRowCount = 0

  private val columnMap = LinkedHashMap<String, ColumnMetaData>()
  private val indexMap = LinkedHashMap<String, IndexMetaData>()
  private val importedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()
  private val exportedForeignKeyMap = LinkedHashMap<String, ForeignKeyMetaData>()

  //
  // Derived values
  //
  override val columns: List<ColumnMetaData> get() = ArrayList(columnMap.values)

  override val indexes: List<IndexMetaData> get() = ArrayList(indexMap.values)

  override val exportedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(exportedForeignKeyMap.values)

  override val importedForeignKeys: List<ForeignKeyMetaData> get() = ArrayList(importedForeignKeyMap.values)

  override val columnCount: Int
    get() = columns.size

  override val primaryKeyColumns: List<ColumnMetaData>
    get() = columns.filter(ColumnMetaData::isPrimaryKey)

  /**
   * {@inheritDoc}
   */
  override fun getColumn(columnName: String): ColumnMetaData? =
    columnMap[columnName.uppercase()]

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

  override fun toString() = tableName

  override fun hashCode() = tableName.uppercase().hashCode()

  override fun equals(other: Any?) = other is TableMetaData && tableName.equals(other.tableName, ignoreCase = true)
}
