package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData


/**
 * Often data types of columns are not compatible: Allow user to define specific mappings.
 */
interface ColumnTypeMapper {
  /**
   * @return target database type including precision and optional not null constraint clause
   */
  fun mapColumnType(column: ColumnMetaData, sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData): String

  fun lookupColumnDefinition(column: ColumnMetaData, sourceDatabase: DatabaseMetaData, targetDatabase: DatabaseMetaData): ColumnDefinition
}
