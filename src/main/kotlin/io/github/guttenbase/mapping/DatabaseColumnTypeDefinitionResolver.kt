package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseSupportedColumnType
import io.github.guttenbase.meta.STANDARD_TYPES

/**
 * Resolve column type definition by looking for the best matching supported type of the target DB
 * as declared in [java.sql.DatabaseMetaData#getTypeInfo]
 *
 * &copy; 2012-2044 tech@spree
 */
object DatabaseColumnTypeDefinitionResolver : AbstractSupportedTypeResolver() {
  override fun match(possibleTypes: List<DatabaseSupportedColumnType>, typeName: String): DatabaseSupportedColumnType? {
    // 1. Prefer standard types over proprietary types
    // 2. Prefer types with highest precision
    return possibleTypes.firstOrNull { it.typeName in STANDARD_TYPES }
      ?: possibleTypes.firstOrNull { it.typeName.contains(typeName) || typeName.contains(it.typeName) }
      ?: possibleTypes.maxByOrNull { it.maxPrecision }
  }
}

