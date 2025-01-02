package io.github.guttenbase.meta

import java.io.Serializable

const val SYNTHETIC_CONSTRAINT_PREFIX = "FK_"

/**
 * Information about a foreign key between table columns.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
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

val ForeignKeyMetaData.databaseType get() = tableMetaData.databaseType
val ForeignKeyMetaData.connectorId get() = tableMetaData.connectorId
val ForeignKeyMetaData.connectorRepository get() = tableMetaData.connectorRepository
