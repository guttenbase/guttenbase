package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

class TestTableMapper : DefaultTableMapper() {
  override fun map(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): TableMetaData {
    val tableName = source.tableName.uppercase().replace("Ö", "O").replace("Ä", "A").replace("Ü", "U")
      .replace(" ", "_")
    return targetDatabaseMetaData.getTable(tableName)?: throw IllegalStateException("$tableName not found")
  }
}
