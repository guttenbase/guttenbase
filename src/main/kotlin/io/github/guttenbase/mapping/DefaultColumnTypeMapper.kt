package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData

/**
 * By default, try to map via supported types in DB
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
object DefaultColumnTypeMapper : AbstractColumnTypeMapper() {
  override fun lookupColumnDefinition(
    sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData, column: ColumnMetaData
  ): ColumnDefinition? {
    val type = targetDatabase.typeFor(column.jdbcColumnType)

    return if (type != null) {
      ColumnDefinition(column, type.typeName, column.precision, column.scale, type.mayUsePrecision)
    } else
      return null
  }
}
