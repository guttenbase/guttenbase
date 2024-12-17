package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.PRECISION_PLACEHOLDER

/**
 * Default uses same data type as source
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractColumnTypeMapper : ColumnTypeMapper {
  /**
   * @return target database type including precision and optional not null, autoincrement, and primary key constraint clauses
   */
  override fun mapColumnType(
    column: ColumnMetaData, sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData
  ): String {
    val columnDefinition = lookupColumnDefinition(column, sourceDatabase, targetDatabase)
    val singlePrimaryKey = column.isPrimaryKey && column.tableMetaData.primaryKeyColumns.size < 2
    val autoincrementClause =
      if (column.isAutoIncrement) " " + targetDatabase.databaseType.createColumnAutoincrementClause(column) else ""
    val notNullClause = if (column.isNullable || singlePrimaryKey) "" else " NOT NULL" // Primary key implies NOT NULL
    val primaryKeyClause = if (singlePrimaryKey) " PRIMARY KEY" else ""
    val defaultValueClause = targetDatabase.databaseType.createDefaultValueClause(columnDefinition) ?: ""

    return columnDefinition.toString() + " $defaultValueClause".trim() + notNullClause + autoincrementClause + primaryKeyClause
  }

  override fun lookupColumnDefinition(
    column: ColumnMetaData, sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData
  ) = lookupColumnDefinitionInternal(sourceDatabase, targetDatabase, column)
    ?: ColumnDefinition(column, column.columnTypeName, column.precision)

  private fun lookupColumnDefinitionInternal(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnDefinition? {
    if (column.isAutoIncrement) {
      val columnType = targetDatabase.databaseType.createColumnAutoIncrementType(column)

      if (columnType != null) {
        return ColumnDefinition(column, columnType)
      }
    }

    return lookupColumnDefinition(sourceDatabase, targetDatabase, column)
  }

  protected abstract fun lookupColumnDefinition(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnDefinition?
}

data class ColumnDefinition(
  val sourceColumn: ColumnMetaData, val targetType: String,
  val precision: Int = 0, val scale: Int = 0,
  val usePrecision: Boolean = false
) {
  val precisionClause: String
    get() {
      val precisionClause = StringBuilder()

      if (usePrecision) {
        precisionClause.append("($precision")

        if (scale > 0) {
          precisionClause.append(", $scale")
        }

        precisionClause.append(")")
      }

      return precisionClause.toString()
    }

  override fun toString(): String {
    val result = if (!targetType.contains(PRECISION_PLACEHOLDER) && usePrecision) {
      targetType + PRECISION_PLACEHOLDER
    } else {
      targetType
    }

    return result.replace(PRECISION_PLACEHOLDER, precisionClause)
  }
}
