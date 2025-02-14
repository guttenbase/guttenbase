package io.github.guttenbase.meta

const val SYNTHETIC_CONSTRAINT_PREFIX = "FK_"

/**
 * Information about a foreign key between table columns.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ForeignKeyMetaData : Comparable<ForeignKeyMetaData>, java.io.Serializable, MetaData {
  val foreignKeyName: String
  val table: TableMetaData
  val referencingColumns: List<ColumnMetaData>
  val referencedColumns: List<ColumnMetaData>
  val referencingTable: TableMetaData
  val referencedTable: TableMetaData
}

val ForeignKeyMetaData.databaseType get() = table.databaseType
val ForeignKeyMetaData.connectorId get() = table.connectorId
val ForeignKeyMetaData.connectorRepository get() = table.connectorRepository
