package io.github.guttenbase.mapping

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.ColumnMetaData
import java.sql.Types

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
    column: ColumnMetaData, sourceDatabaseType: DatabaseType, targetDatabaseType: DatabaseType
  ): String {
    val columnDefinition = lookupColumnDefinitionInternal(sourceDatabaseType, targetDatabaseType, column)
      ?: createDefaultColumnDefinition(column)
    val precision = createPrecisionClause(column, columnDefinition.precision)
    val singlePrimaryKey = column.isPrimaryKey && column.tableMetaData.primaryKeyColumns.size < 2
    val autoincrementClause =
      if (column.isAutoIncrement) " " + lookupAutoIncrementClause(column, targetDatabaseType) else ""
    val notNullClause = if (column.isNullable || singlePrimaryKey) "" else " NOT NULL" // Primary key implies NOT NULL
    val primaryKeyClause = if (singlePrimaryKey) " PRIMARY KEY" else ""
    val defaultValueClause = targetDatabaseType.lookupDefaultValueClause(columnDefinition)

    return columnDefinition.type + precision + defaultValueClause + notNullClause + autoincrementClause + primaryKeyClause
  }

  /**
   * Override this method if you just want to change the way column types are logically mapped
   *
   * @return target database type including precision
   */
  @Suppress("SameParameterValue")
  protected fun createDefaultColumnDefinition(
    columnMetaData: ColumnMetaData, precision: String = ""
  ): ColumnDefinition {
    val precisionClause = createPrecisionClause(columnMetaData, precision)
    val defaultColumnType: String = columnMetaData.columnTypeName

    return ColumnDefinition(defaultColumnType, precisionClause)
  }

  protected fun createPrecisionClause(columnMetaData: ColumnMetaData, optionalPrecision: String): String {
    return when (columnMetaData.columnType) {
      Types.CHAR, Types.VARCHAR, Types.VARBINARY -> if (columnMetaData.columnTypeName.uppercase().contains("TEXT")) {
        "" // TEXT does not support precision
      } else {
        val precision = columnMetaData.precision

        if (precision > 0) "($precision)" else ""
      }

      else -> optionalPrecision
    }
  }

  private fun lookupColumnDefinitionInternal(
    sourceDatabaseType: DatabaseType, targetDatabaseType: DatabaseType, column: ColumnMetaData
  ): ColumnDefinition? {
    if (column.isAutoIncrement) {
      val columnType = targetDatabaseType.createColumnAutoIncrementType(column)

      if (columnType != null) {
        return ColumnDefinition(columnType, "")
      }
    }

    return lookupColumnDefinition(sourceDatabaseType, targetDatabaseType, column)
  }

  protected abstract fun lookupColumnDefinition(
    sourceDatabaseType: DatabaseType,
    targetDatabaseType: DatabaseType,
    column: ColumnMetaData
  ): ColumnDefinition?

  /**
   * ID columns may be defined as autoincremented, i.e. every time data is inserted the ID will be incremented autoimatically.
   * Unfortunately every database has its own way to implement this feature.
   *
   * @return the autoincrement clause for the target database
   */
  protected fun lookupAutoIncrementClause(column: ColumnMetaData, targetDatabaseType: DatabaseType) =
    targetDatabaseType.createColumnAutoincrementClause(column)
}

data class ColumnDefinition(val type: String, val precision: String)
