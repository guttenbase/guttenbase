package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.InternalIndexMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * Information about index in table.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class IndexMetaDataImpl(
  override val tableMetaData: TableMetaData,
  override val indexName: String,
  override val isAscending: Boolean,
  override val isUnique: Boolean,
  override val isPrimaryKeyIndex: Boolean
) : InternalIndexMetaData {
  private val _columns: MutableList<ColumnMetaData> = ArrayList()

  override val columnMetaData: List<ColumnMetaData> get() = ArrayList(_columns)

  override fun addColumn(columnMetaData: ColumnMetaData) {
    _columns.add(columnMetaData)
  }

  override operator fun compareTo(other: IndexMetaData) = indexName.uppercase().compareTo(other.indexName.uppercase())

  override fun toString() = "$tableMetaData:$indexName:$columnMetaData"

  override fun hashCode() = indexName.uppercase().hashCode()

  override fun equals(other: Any?): Boolean {
    return if (other is IndexMetaData) {
      indexName.equals(other.indexName, ignoreCase = true)
    } else {
      false
    }
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
