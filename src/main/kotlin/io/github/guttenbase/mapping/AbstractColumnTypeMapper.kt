package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData

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
    val primaryKeyClause = if (singlePrimaryKey) " PRIMARY KEY NOT NULL" else ""
    val defaultValueClause = targetDatabase.databaseType.createDefaultValueClause(columnDefinition) ?: ""

    return "$columnDefinition$defaultValueClause$notNullClause$autoincrementClause$primaryKeyClause"
  }

  override fun lookupColumnDefinition(
    column: ColumnMetaData, sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData
  ) = lookupColumnDefinitionInternal(sourceDatabase, targetDatabase, column)

  private fun lookupColumnDefinitionInternal(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnTypeDefinition {
    if (column.isAutoIncrement) {
      val columnType = targetDatabase.databaseType.createColumnAutoIncrementType(column)

      if (columnType != null) {
        return ColumnTypeDefinition(column, targetDatabase, columnType.typeName)
      }
    }

    return lookupColumnDefinition(sourceDatabase, targetDatabase, column)
  }

  protected abstract fun lookupColumnDefinition(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnTypeDefinition
}

