package io.github.guttenbase.defaults.impl

import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.ColumnMapper.ColumnMapperResult
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * By default, return column with same name ignoring case.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultColumnMapper @JvmOverloads constructor(
  private val caseConversionMode: CaseConversionMode = CaseConversionMode.NONE
) : ColumnMapper {
  override fun mapColumnName(source: ColumnMetaData, targetTableMetaData: DatabaseEntityMetaData): String {
    return caseConversionMode.convert(source.columnName)
  }

  override fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapperResult {
    val columnMetaData = targetTableMetaData.getColumn(source.columnName)
    val result = if (columnMetaData != null) listOf(columnMetaData) else ArrayList()

    return ColumnMapperResult(result)
  }
}
