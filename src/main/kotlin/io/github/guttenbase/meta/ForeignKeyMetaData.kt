package io.github.guttenbase.meta

import java.io.Serializable

/**
 * Information about a foreign key between table columns.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ForeignKeyMetaData : Comparable<ForeignKeyMetaData>, Serializable {
  val foreignKeyName: String
  val tableMetaData: TableMetaData
  val referencingColumns: List<ColumnMetaData>
  val referencedColumns: List<ColumnMetaData>
  val referencingTableMetaData: TableMetaData
  val referencedTableMetaData: TableMetaData
}
