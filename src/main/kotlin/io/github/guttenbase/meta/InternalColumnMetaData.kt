package io.github.guttenbase.meta

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalColumnMetaData : ColumnMetaData {
  override var table: TableMetaData

  override var isPrimaryKey: Boolean

  override var isGenerated: Boolean

  override var columnSize: Int

  override var defaultValue: String?
}