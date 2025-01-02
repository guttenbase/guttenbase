@file:Suppress("unused")

package io.github.guttenbase.meta

import java.io.Serializable

const val SYNTHETIC_INDEX_PREFIX = "IDX_"

/**
 * Information about index in table.
 *
 *  &copy; 2012-2034 akquinet tech@spree
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

val IndexMetaData.databaseType get() = tableMetaData.databaseType
val IndexMetaData.connectorId get() = tableMetaData.connectorId
val IndexMetaData.connectorRepository get() = tableMetaData.connectorRepository
