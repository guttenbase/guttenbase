package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ForeignKeyMetaData
import io.github.guttenbase.meta.InternalForeignKeyMetaData
import io.github.guttenbase.meta.TableMetaData
import org.slf4j.LoggerFactory

/**
 * Information about a foreign key between table columns.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ForeignKeyMetaDataImpl(
  override val tableMetaData: TableMetaData,
  override val foreignKeyName: String,
  referencingColumns: List<ColumnMetaData>,
  referencedColumns: List<ColumnMetaData>
) : InternalForeignKeyMetaData {
  private val referencingColumnData = ArrayList(referencingColumns)
  private val referencedColumnData = ArrayList(referencedColumns)

  constructor(
    tableMetaData: TableMetaData,
    foreignKeyName: String,
    referencingColumn: ColumnMetaData,
    referencedColumn: ColumnMetaData
  ) : this(tableMetaData, foreignKeyName, listOf(referencingColumn), listOf(referencedColumn))

  override val referencingTableMetaData: TableMetaData
    get() {
      assert(referencingColumns.isNotEmpty()) { "no referencing columns" }
      return referencingColumns[0].tableMetaData
    }

  override val referencedTableMetaData: TableMetaData
    get() {
      assert(referencedColumns.isNotEmpty()) { "no referenced columns" }
      return referencedColumns[0].tableMetaData
    }

  override val referencingColumns: List<ColumnMetaData> get() = ArrayList(referencingColumnData)

  override val referencedColumns: List<ColumnMetaData> get() = ArrayList(referencedColumnData)

  override fun addColumnTuple(referencingColumn: ColumnMetaData, referencedColumn: ColumnMetaData) {
    if (referencingColumnData.contains(referencingColumn)) {
      LOG.warn("Referencing column already added: $referencingColumn")
    } else {
      referencingColumnData.add(referencingColumn)
    }

    if (referencedColumnData.contains(referencedColumn)) {
      LOG.warn("Referenced column already added: $referencedColumn")
    } else {
      referencedColumnData.add(referencedColumn)
    }
  }

  override operator fun compareTo(other: ForeignKeyMetaData) =
    foreignKeyName.uppercase().compareTo(other.foreignKeyName.uppercase())

  override fun toString() = "$tableMetaData:$foreignKeyName:$referencingColumns->$referencedColumns"

  override fun hashCode() = foreignKeyName.uppercase().hashCode()

  override fun equals(other: Any?): Boolean {
    val that: ForeignKeyMetaData = other as ForeignKeyMetaData
    return foreignKeyName.equals(that.foreignKeyName, ignoreCase = true)
  }

  companion object {
    private const val serialVersionUID = 1L

    @JvmStatic
    private val LOG = LoggerFactory.getLogger(ForeignKeyMetaDataImpl::class.java)
  }
}
