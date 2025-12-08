package io.github.guttenbase.repository

import io.github.guttenbase.meta.ForeignKeyMetaData

/**
 * Filter foreign keys when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
fun interface DatabaseForeignKeyFilter {
  /**
   * Perform custom check on column before adding it to column meta data
   */
  fun accept(foreignKeyMetaData: ForeignKeyMetaData): Boolean
}
