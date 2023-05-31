package io.github.guttenbase.repository.impl

import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType

/**
 * Try to resolve by JDBC class name.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ClassNameColumnTypeResolver : ColumnTypeResolver {
  /**
   * Try to resolve by JDBC class name.
   */
  override fun getColumnType(columnMetaData: ColumnMetaData): ColumnType {
    val columnClassName: String = columnMetaData.columnClassName
    return if (ColumnType.isSupportedClass(columnClassName)) ColumnType.valueForClass(columnClassName) else ColumnType.CLASS_UNKNOWN
  }
}
