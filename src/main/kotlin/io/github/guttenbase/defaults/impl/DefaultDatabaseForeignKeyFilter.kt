package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ForeignKeyMetaData
import io.github.guttenbase.repository.DatabaseForeignKeyFilter

/**
 * Filter foreign keys when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultDatabaseForeignKeyFilter : DatabaseForeignKeyFilter {
  override fun accept(foreignKeyMetaData: ForeignKeyMetaData) = true
}
