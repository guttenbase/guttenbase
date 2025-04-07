package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.DatabaseMetaData

class TestTableNameMapper : DefaultTableMapper() {
  override fun fullyQualifiedTableName(source: DatabaseEntityMetaData, targetDatabaseMetaData: DatabaseMetaData) =
    "\"" + mapTableName(source, targetDatabaseMetaData) + "\""
}
