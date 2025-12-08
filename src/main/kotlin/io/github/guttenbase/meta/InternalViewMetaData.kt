package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2025-2044 tech@spree
 *
 * @author M. Dahm
 */
interface InternalViewMetaData : ViewMetaData, InternalDatabaseEntityMetaData {
  override var viewDefinition: String
}