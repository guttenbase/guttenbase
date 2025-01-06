package io.github.guttenbase.repository.impl

import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.Companion.isSupported

/**
 * Try to resolve by JDBC type.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object JDBCColumnTypeResolver : ColumnTypeResolver {
  /**
   * Try to resolve by JDBC class name.
   */
  override fun getColumnType(columnMetaData: ColumnMetaData): ColumnType? {
    val type = columnMetaData.jdbcColumnType

    return if (type.isSupported()) ColumnType.valueOf(type) else null
  }
}
