package io.github.guttenbase.repository

import io.github.guttenbase.meta.IndexMetaData

/**
 * Filter indexes when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface DatabaseIndexFilter {
  /**
   * Perform custom check on column before adding it to column meta data
   */
  fun accept(indexMetaData: IndexMetaData): Boolean
}
