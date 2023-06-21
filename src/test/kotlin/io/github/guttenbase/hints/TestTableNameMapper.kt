package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

class TestTableNameMapper : DefaultTableMapper() {
  override fun fullyQualifiedTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData) =
    "\"" + mapTableName(source, targetDatabaseMetaData) + "\""
}
