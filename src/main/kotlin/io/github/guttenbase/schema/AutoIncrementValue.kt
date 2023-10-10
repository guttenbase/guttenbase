package io.github.guttenbase.schema

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Determine start value and step factor for autoincrement (aka IDENTITY) columns
 *
 * Using this hint the @see [io.github.guttenbase.mapping.DefaultColumnTypeMapper] generate a clause with the given start value and step factor.
 */
interface AutoIncrementValue {
  fun startValue(columnMetaData: ColumnMetaData): Long

  fun stepWidth(columnMetaData: ColumnMetaData): Long
}
