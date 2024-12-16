package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.InternalColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import java.sql.JDBCType
import java.util.*

/**
 * Information about a table column.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ColumnMetaDataImpl(
  override val tableMetaData: TableMetaData,
  override val columnType: Int,
  override val columnName: String,
  override val columnTypeName: String,
  override val columnClassName: String,
  override val isNullable: Boolean,
  override val isAutoIncrement: Boolean,
  override val precision: Int,
  override val scale: Int
) : InternalColumnMetaData {
  constructor(tableMetaData: TableMetaData, columnMetaData: ColumnMetaData) : this(
    tableMetaData,
    columnMetaData.columnType, columnMetaData.columnName, columnMetaData.columnTypeName, columnMetaData.columnClassName,
    columnMetaData.isNullable, columnMetaData.isAutoIncrement, columnMetaData.precision, columnMetaData.scale
  )

  override val jdbcColumnType: JDBCType = JDBCType.valueOf(columnType)

  /**
   * {@inheritDoc}
   */
  override var isPrimaryKey = false

  /**
   * / ** {@inheritDoc}
   */
  override val columnId = UUID.randomUUID()!!

  override val referencedColumns: Map<String, List<ColumnMetaData>>
    get() = tableMetaData.importedForeignKeys.filter { it.referencingColumns.contains(this) }
      .associate { it.foreignKeyName to it.referencedColumns }

  override val referencingColumns: Map<String, List<ColumnMetaData>>
    get() = tableMetaData.exportedForeignKeys.filter { it.referencedColumns.contains(this) }
      .associate { it.foreignKeyName to it.referencingColumns }

  override operator fun compareTo(other: ColumnMetaData) =
    columnName.uppercase().compareTo(other.columnName.uppercase())

  override fun toString() = "$tableMetaData:$columnName:$columnTypeName"

  override fun hashCode() = columnName.uppercase().hashCode()

  override fun equals(other: Any?) = other is ColumnMetaData && columnName.equals(other.columnName, ignoreCase = true)

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}
