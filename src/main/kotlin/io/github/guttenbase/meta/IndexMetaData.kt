package io.github.guttenbase.meta

import java.io.Serializable

/**
 * Information about index in table.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface IndexMetaData : Comparable<IndexMetaData>, Serializable {
  val indexName: String
  val isAscending: Boolean
  val isUnique: Boolean
  val columnMetaData: List<ColumnMetaData>
  val tableMetaData: TableMetaData
  val isPrimaryKeyIndex: Boolean
}
