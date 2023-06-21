package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.repository.DatabaseColumnFilter

open class DefaultDatabaseColumnFilter : DatabaseColumnFilter {
  override fun accept(columnMetaData: ColumnMetaData) = true
}
