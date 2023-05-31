package io.github.guttenbase.defaults.impl

import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.ColumnMapper.ColumnMapperResult
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * By default, return column with same name ignoring case. You may however configure case and escaping the column names explicitely.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultColumnMapper @JvmOverloads constructor(
  private val caseConversionMode: CaseConversionMode = CaseConversionMode.NONE,
  private val escapeCharacter: String = ""
) : ColumnMapper {
  override fun mapColumnName(source: ColumnMetaData, targetTableMetaData: TableMetaData): String {
    return escapeCharacter + caseConversionMode.convert(source.columnName) + escapeCharacter
  }

  override fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapperResult {
    val columnMetaData = targetTableMetaData.getColumnMetaData(source.columnTypeName)
    val result = if (columnMetaData != null) listOf(columnMetaData) else ArrayList()

    return ColumnMapperResult(result)
  }
}
