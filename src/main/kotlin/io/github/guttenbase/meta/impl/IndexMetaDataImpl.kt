package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.InternalIndexMetaData
import io.github.guttenbase.meta.TableMetaData
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Information about index in table.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Serializable
class IndexMetaDataImpl(
  @Transient
  override val tableMetaData: TableMetaData = DUMMYTABLE, // TODO
  override val indexName: String,
  override val isAscending: Boolean,
  override val isUnique: Boolean,
  override val isPrimaryKeyIndex: Boolean
) : InternalIndexMetaData {
  private val columns: MutableList<ColumnMetaData> = ArrayList()

  override val columnMetaData: List<ColumnMetaData> get() = ArrayList(columns)

  override fun addColumn(columnMetaData: ColumnMetaData) {
    columns.add(columnMetaData)
  }

  override operator fun compareTo(other: IndexMetaData) = indexName.uppercase().compareTo(other.indexName.uppercase())

  override fun toString() = "$tableMetaData:$indexName:$columnMetaData"

  override fun hashCode() = indexName.uppercase().hashCode()

  override fun equals(other: Any?) = other is IndexMetaData && indexName.equals(other.indexName, ignoreCase = true)

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}
