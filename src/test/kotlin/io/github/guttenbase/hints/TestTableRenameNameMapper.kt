package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * look at columnName starts with some value and replace them
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class TestTableRenameNameMapper @JvmOverloads constructor(
  private val caseConversionMode: CaseConversionMode = CaseConversionMode.NONE,
  private val addSchema: Boolean = true
) : TableMapper {
  private val replacementsTables = HashMap<String, String>()

  override fun map(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): TableMetaData {
    val defaultTableName = caseConversionMode.convert(source.tableName)
    val tableName =      if (replacementsTables.containsKey(defaultTableName)) replacementsTables[defaultTableName]!! else defaultTableName
    return targetDatabaseMetaData.getTableMetaData(tableName)!!
  }

  override fun mapTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): String {
    var result = caseConversionMode.convert(source.tableName)
    val schema = targetDatabaseMetaData.schema
    val tableName = replacementsTables[result]

    if (tableName != null) {
      result = caseConversionMode.convert(tableName)
    }

    return if ("" == schema.trim { it <= ' ' } || !addSchema) {
      result
    } else {
      "$schema.$result"
    }
  }

  fun addReplacement(sourceTable: String, targetTable: String): TestTableRenameNameMapper {
    replacementsTables[sourceTable] = targetTable
    return this
  }

  override fun fullyQualifiedTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData) =
    mapTableName(source, targetDatabaseMetaData)
}
