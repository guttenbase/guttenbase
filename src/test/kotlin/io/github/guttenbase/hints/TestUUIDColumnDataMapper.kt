package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.meta.ColumnMetaData
import java.util.*

/**
 * Convert long ID to UUID
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TestUUIDColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?): Any? {
    val number = value as Number?

    return if (number != null) {
      val id = number.toLong()
      val sourceColumnMetaData = mapping.sourceColumn
      val iterator = sourceColumnMetaData.referencedColumns.values.iterator()
      val referencedColumn = if (iterator.hasNext()) iterator.next()[0] else sourceColumnMetaData

      createKey(referencedColumn, id)
    } else {
      null
    }
  }

  override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData): Boolean {
    val sourceColumnName = sourceColumnMetaData.columnName.uppercase()
    val targetColumnName = targetColumnMetaData.columnName.uppercase()

    return sourceColumnName == targetColumnName && sourceColumnName.endsWith("ID")
  }

  /**
   * Very simple way to create UUID. We create it from the column's hash code and the old id. You might want to use something mor
   * complicated.
   */
  private fun createKey(columnMetaData: ColumnMetaData, id: Long): String {
    val key = columnMetaData.container.tableName + ":" + columnMetaData.columnName
    val uuid = UUID(key.hashCode().toLong(), id)

    return uuid.toString()
  }
}
