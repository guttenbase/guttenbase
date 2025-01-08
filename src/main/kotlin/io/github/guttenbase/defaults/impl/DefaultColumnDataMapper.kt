package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.meta.isStringType

/**
 * By default always just return the same object.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultColumnDataMapper : ColumnDataMapper {
  override fun map(mapping: ColumnDataMapping, value: Any?) =
    if (mapping.targetColumn.databaseType == DatabaseType.ORACLE
      && mapping.targetColumn.jdbcColumnType.isStringType() && !mapping.targetColumn.isNullable
      && (value == null || "" == value)
    ) {
      // Oracle has a weird concept of empty strings, they are treated as NULL
      // https://stackoverflow.com/questions/13278773/null-vs-empty-string-in-oracle
      " "
    } else {
      value
    }
}
