package io.github.guttenbase.repository

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Regard which columns when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
fun interface DatabaseColumnFilter {
  /**
   * Perform custom check on column before adding it to table meta data
   */
  fun accept(columnMetaData: ColumnMetaData): Boolean
}
