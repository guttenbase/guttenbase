package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultColumnMapper
import io.github.guttenbase.mapping.ColumnMapper.ColumnMapperResult
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData

class TestTableColumnMapper : DefaultColumnMapper() {
  override fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapperResult {
    val columnName = source.columnName

    return if (columnName.equals("ID", ignoreCase = true)) {
      val newColumnName = mapColumnName(source)
      val columnMetaData = targetTableMetaData.getColumn(newColumnName)
      val result = if (columnMetaData != null) listOf(columnMetaData) else ArrayList()

      ColumnMapperResult(result)
    } else {
      super.map(source, targetTableMetaData)
    }
  }

  private fun mapColumnName(columnMetaData: ColumnMetaData) =
    columnMetaData.table.tableName.substring("FOO_".length) + "_ID"
}
