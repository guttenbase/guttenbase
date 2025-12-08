package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.repository.DatabaseColumnFilter

/**
 * Regard which columns when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
open class DefaultDatabaseColumnFilter : DatabaseColumnFilter {
  override fun accept(columnMetaData: ColumnMetaData) = true
}
