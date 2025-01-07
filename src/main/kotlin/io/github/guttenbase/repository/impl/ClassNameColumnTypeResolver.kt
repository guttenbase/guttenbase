package io.github.guttenbase.repository.impl

import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.Companion.isSupportedColumnType

/**
 * Try to resolve by JDBC class name.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object ClassNameColumnTypeResolver : ColumnTypeResolver {
  /**
   * Try to resolve by JDBC class name.
   */
  override fun getColumnType(columnMetaData: ColumnMetaData): ColumnType? {
    val columnClassName = columnMetaData.columnClassName

    return if (columnClassName.isSupportedColumnType()) ColumnType.valueOfClassName(columnClassName) else null
  }
}
