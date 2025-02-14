package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.InternalIndexMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.UUID

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
  override var table: TableMetaData = TABLE_FOR_SERIALIZATION,
  override val indexName: String,
  override val isAscending: Boolean,
  override val isUnique: Boolean,
  override val isPrimaryKeyIndex: Boolean
) : InternalIndexMetaData {
  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val syntheticId = UUID.randomUUID()!!

  private val columnData: MutableList<ColumnMetaData> = ArrayList()

  override val columns: List<ColumnMetaData> get() = ArrayList(columnData)

  override fun addColumn(column: ColumnMetaData) {
    columnData.add(column)
  }

  override fun clearColumns() {
    columnData.clear()
  }

  override operator fun compareTo(other: IndexMetaData) = indexName.uppercase().compareTo(other.indexName.uppercase())

  override fun toString() = "$table:$indexName:$columns"

  override fun hashCode() = indexName.uppercase().hashCode()

  override fun equals(other: Any?) = other is IndexMetaData && indexName.equals(other.indexName, ignoreCase = true)
}
