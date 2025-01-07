package io.github.guttenbase.defaults.impl

import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * By default prepend schema name.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultTableMapper @JvmOverloads constructor(private val caseConversionMode: CaseConversionMode = CaseConversionMode.NONE) :
  TableMapper {

  override fun map(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData): TableMetaData? {
    val tableName = mapTableName(source, targetDatabaseMetaData)
    return targetDatabaseMetaData.getTableMetaData(tableName)
  }

  override fun fullyQualifiedTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData) =
    targetDatabaseMetaData.schemaPrefix + targetDatabaseMetaData.databaseType
      .escapeDatabaseEntity(mapTableName(source, targetDatabaseMetaData))

  override fun mapTableName(source: TableMetaData, targetDatabaseMetaData: DatabaseMetaData) =
    caseConversionMode.convert(source.tableName)
}
