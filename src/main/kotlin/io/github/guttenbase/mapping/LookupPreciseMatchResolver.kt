package io.github.guttenbase.mapping

import io.github.guttenbase.meta.DatabaseSupportedColumnType

/**
 * Resolve column type definition looking for an exact match in the target database.
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
object LookupPreciseMatchResolver : AbstractSupportedTypeResolver() {
  override fun match(possibleTypes: List<DatabaseSupportedColumnType>, typeName: String): DatabaseSupportedColumnType? =
    possibleTypes.firstOrNull { it.typeName == typeName }
}