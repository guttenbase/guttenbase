package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ForeignKeyMetaData
import io.github.guttenbase.meta.InternalForeignKeyMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.utils.Util.immutable
import org.slf4j.LoggerFactory

/**
 * Information about a foreign key between table columns.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ForeignKeyMetaDataImpl(
  override val tableMetaData: TableMetaData,
  override val foreignKeyName: String,
  referencingColumn: ColumnMetaData,
  referencedColumn: ColumnMetaData
) : InternalForeignKeyMetaData {
  private val _referencingColumns: MutableList<ColumnMetaData> = mutableListOf(referencingColumn)
  private val _referencedColumns: MutableList<ColumnMetaData> = mutableListOf(referencedColumn)

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

  override val referencedColumns: List<ColumnMetaData> by immutable(_referencedColumns)
  override val referencingColumns: List<ColumnMetaData> by immutable(_referencingColumns)

  override fun addColumnTuple(referencingColumn: ColumnMetaData, referencedColumn: ColumnMetaData) {
    if (_referencingColumns.contains(referencingColumn)) {
      LOG.warn("Referencing column already added: $referencingColumn")
    } else {
      _referencingColumns.add(referencingColumn)
    }

    if (_referencedColumns.contains(referencedColumn)) {
      LOG.warn("Referenced column already added: $referencedColumn")
    } else {
      _referencedColumns.add(referencedColumn)
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
    private val LOG = LoggerFactory.getLogger(ForeignKeyMetaDataImpl::class.java)
  }
}
