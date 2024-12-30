package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseSupportedColumnType
import io.github.guttenbase.meta.STANDARD_TYPES
import java.sql.JDBCType

/**
 * Resolve column type definition by looking for the best matching supported type of the target DB
 * as declared in [java.sql.DatabaseMetaData#getTypeInfo]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object DatabaseColumnTypeDefinitionResolver : ColumnTypeDefinitionResolver {
  override fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition? {
    val result = type.targetDataBase.typeFor(type.jdbcType)

    return if (result != null) {
      val precision = computePrecision(type.precision, result)

      ColumnTypeDefinition(type.sourceColumn, type.targetDataBase, result.typeName, type.jdbcType, precision, type.scale)
    } else {
      null
    }
  }
}

private fun DatabaseMetaData.typeFor(jdbcType: JDBCType): DatabaseSupportedColumnType? {
  val possibleTypes = supportedTypes[jdbcType] ?: listOf<DatabaseSupportedColumnType>()

  // 1. Prefer standard types over proprietary types
  // 2. Prefer types with highest precision
  return possibleTypes.firstOrNull { it.typeName in STANDARD_TYPES } ?: possibleTypes.maxByOrNull { it.maxPrecision }
}

