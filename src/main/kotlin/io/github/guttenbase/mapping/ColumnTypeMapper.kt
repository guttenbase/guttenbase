package io.github.guttenbase.mapping

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.ColumnMetaData


/**
 * Often data types of columns are not compatible: Allow user to define specific mappings.
 */
fun interface ColumnTypeMapper {
  /**
   * @return target database type including precision and optional not null constraint clause
   */
  fun mapColumnType(columnMetaData: ColumnMetaData, sourceDatabaseType: DatabaseType, targetDatabaseType: DatabaseType): String
}
