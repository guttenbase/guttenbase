package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * look if columnName starts with some value and replace it
 *
 *
 * Created by Marynasuprun on 26.03.2017.
 */
class TestColumnRenameNameMapper : ColumnMapper {
  private val replacementsColumns = HashMap<String, String>()

  override fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapper.ColumnMapperResult {
    val defaultColumnName = source.columnName
    val columnName = replacementsColumns.getOrDefault(defaultColumnName, defaultColumnName)
    val columnMetaData2 = targetTableMetaData.getColumn(columnName)!!

    return ColumnMapper.ColumnMapperResult(listOf(columnMetaData2))
  }

  override fun mapColumnName(source: ColumnMetaData, targetTableMetaData: DatabaseEntityMetaData): String {
    val result = source.columnName
    val columnName = replacementsColumns[result]

    return columnName ?: result
  }

  @Suppress("unused")
  fun addReplacement(sourceComn: String, targetColumn: String): TestColumnRenameNameMapper {
    replacementsColumns[sourceComn] = targetColumn
    return this
  }
}
