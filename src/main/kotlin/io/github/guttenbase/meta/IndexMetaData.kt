@file:Suppress("unused")

package io.github.guttenbase.meta

const val SYNTHETIC_INDEX_PREFIX = "IDX_"

/**
 * Information about index in table.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
interface IndexMetaData : Comparable<IndexMetaData>, MetaData  {
  val indexName: String
  val isAscending: Boolean
  val isUnique: Boolean
  val columns: List<ColumnMetaData>
  val table: TableMetaData
  val isPrimaryKeyIndex: Boolean
}

val IndexMetaData.databaseType get() = table.databaseType
val IndexMetaData.connectorId get() = table.connectorId
val IndexMetaData.connectorRepository get() = table.connectorRepository
