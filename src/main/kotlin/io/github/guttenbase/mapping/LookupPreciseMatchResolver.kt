package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseSupportedColumnType
import java.sql.JDBCType

/**
 * Resolve column type definition looking for an exact match in the target database.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
object LookupPreciseMatchResolver : ColumnTypeDefinitionResolver {
  override fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition? {
    val result = type.targetDataBase.typeFor(type.jdbcType, type.typeName)

    return if (result != null) {
      val precision = computePrecision(type.precision, result)

      ColumnTypeDefinition(type.sourceColumn, type.targetDataBase, result.typeName, type.jdbcType, precision, type.scale)
    } else {
      null
    }
  }
}

private fun DatabaseMetaData.typeFor(jdbcType: JDBCType, typeName: String): DatabaseSupportedColumnType? {
  val possibleTypes = supportedTypes[jdbcType] ?: listOf<DatabaseSupportedColumnType>()

  return possibleTypes.firstOrNull { it.typeName == typeName }
}
