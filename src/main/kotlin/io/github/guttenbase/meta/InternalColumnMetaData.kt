package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalColumnMetaData : ColumnMetaData {
  override var container: DatabaseEntityMetaData

  override var isPrimaryKey: Boolean

  override var isGenerated: Boolean

  override var columnSize: Int

  override var defaultValue: String?
}