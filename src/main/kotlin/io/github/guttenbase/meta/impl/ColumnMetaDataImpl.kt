package io.github.guttenbase.meta.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.InternalColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.hasJDBCType
import io.github.guttenbase.serialization.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.sql.JDBCType
import java.util.*

/**
 * Information about a table column.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Serializable
class ColumnMetaDataImpl(
  @Transient
  override val tableMetaData: TableMetaData = DUMMYTABLE, // TODO
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

  /**
   * {@inheritDoc}
   */
  override var isPrimaryKey = false

  override var isGenerated = false

  override var columnSize = 0

  override var defaultValue: String? = null

  /**
   * {@inheritDoc}
   */
  @Serializable(with = UUIDSerializer::class)
  override val columnId = UUID.randomUUID()!!

  override val referencedColumns: Map<String, List<ColumnMetaData>>
    get() = tableMetaData.importedForeignKeys.filter { it.referencingColumns.contains(this) }
      .associate { it.foreignKeyName to it.referencedColumns }

  override val referencingColumns: Map<String, List<ColumnMetaData>>
    get() = tableMetaData.exportedForeignKeys.filter { it.referencedColumns.contains(this) }
      .associate { it.foreignKeyName to it.referencingColumns }

  override val jdbcColumnType: JDBCType = if (columnType.hasJDBCType()) JDBCType.valueOf(columnType) else JDBCType.OTHER

  override operator fun compareTo(other: ColumnMetaData) =
    columnName.uppercase().compareTo(other.columnName.uppercase())

  override fun toString() = "$tableMetaData:$columnName:$columnTypeName:$jdbcColumnType"

  override fun hashCode() = columnName.uppercase().hashCode()

  override fun equals(other: Any?) = other is ColumnMetaData && columnName.equals(other.columnName, ignoreCase = true)

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}
