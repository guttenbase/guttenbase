package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseSupportedColumnType

/**
 * Abstract super class combining common cod for supported type lookup
 *
 * &copy; 2012-2044 tech@spree
 */
abstract class AbstractSupportedTypeResolver : ColumnTypeDefinitionResolver {
  override fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition? {
    val possibleTypes = type.targetDatabase.supportedTypes[type.jdbcType] ?: listOf<DatabaseSupportedColumnType>()
    val result = match(possibleTypes, type.typeName)

    return if (result != null) {
      val precision = computePrecision(type.precision, result)
      val usePrecision = type.targetDatabase.databaseType.supportsPrecisionClause(result.typeName)

      ColumnTypeDefinition(
        type.sourceColumn, type.targetDatabase, result.typeName, type.jdbcType,
        usePrecision, precision, type.scale
      )
    } else {
      null
    }
  }

  protected abstract fun match(possibleTypes: List<DatabaseSupportedColumnType>, typeName: String): DatabaseSupportedColumnType?
}