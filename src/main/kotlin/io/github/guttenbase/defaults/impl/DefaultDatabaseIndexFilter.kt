package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.SYNTHETIC_CONSTRAINT_PREFIX
import io.github.guttenbase.repository.DatabaseIndexFilter

/**
 * Filter indexes when @see [io.github.guttenbase.repository.impl.DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultDatabaseIndexFilter : DatabaseIndexFilter {
  override fun accept(indexMetaData: IndexMetaData): Boolean {
    val name = indexMetaData.indexName.uppercase()

    return when {
      name.startsWith(SYNTHETIC_CONSTRAINT_PREFIX) -> false
      name.startsWith("PK_") -> false
      name.startsWith("SQL") -> false
      name.startsWith("SYS_") -> false
      name.contains("$") -> false
      else -> true
    }
  }
}
