package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ForeignKeyMetaData
import io.github.guttenbase.meta.InternalForeignKeyMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.serialization.UUIDSerializer
import io.github.guttenbase.utils.Util.RIGHT_ARROW
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.slf4j.LoggerFactory
import java.util.UUID

/**
 * Information about a foreign key between table columns.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
@Serializable
class ForeignKeyMetaDataImpl(
  @Transient
  override var table: TableMetaData = TABLE_FOR_SERIALIZATION,
  override val foreignKeyName: String,
  private val referencingColumnData: MutableList<ColumnMetaData>,
  private val referencedColumnData: MutableList<ColumnMetaData>
) : InternalForeignKeyMetaData {

  constructor(
    tableMetaData: TableMetaData,
    foreignKeyName: String,
    referencingColumn: ColumnMetaData,
    referencedColumn: ColumnMetaData
  ) : this(tableMetaData, foreignKeyName, mutableListOf(referencingColumn), mutableListOf(referencedColumn))

  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val syntheticId = UUID.randomUUID()!!

  override val referencingTable: TableMetaData
    get() {
      assert(referencingColumns.isNotEmpty()) { "no referencing columns" }
      return referencingColumns[0].table
    }

  override val referencedTable: TableMetaData
    get() {
      assert(referencedColumns.isNotEmpty()) { "no referenced columns" }
      return referencedColumns[0].table
    }

  override fun clearReferencingColumns() {
    referencingColumnData.clear()
  }

  override fun clearReferencedColumns() {
    referencedColumnData.clear()
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

  override fun toString() = "$table: $foreignKeyName: $referencingColumns $RIGHT_ARROW $referencedColumns"

  override fun hashCode() = foreignKeyName.uppercase().hashCode()

  override fun equals(other: Any?) =
    other is ForeignKeyMetaData && foreignKeyName.equals(other.foreignKeyName, ignoreCase = true)

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(ForeignKeyMetaDataImpl::class.java)
  }
}
