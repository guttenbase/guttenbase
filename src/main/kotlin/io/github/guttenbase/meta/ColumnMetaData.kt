package io.github.guttenbase.meta

import java.io.Serializable

/**
 * Information about a table column.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ColumnMetaData : Comparable<ColumnMetaData>, Serializable {
  /**
   * Column type as defined in [java.sql.Types]
   */
  val columnType: Int
  val columnName: String
  val columnTypeName: String
  val columnClassName: String

  /**
   * @return containing table
   */
  val tableMetaData: TableMetaData
  val isNullable: Boolean
  val isAutoIncrement: Boolean
  val precision: Int
  val scale: Int
  val isPrimaryKey: Boolean

  /**
   * @return referenced columns for each foreign key constraint
   */
  val referencedColumns: Map<String, List<ColumnMetaData>>

  /**
   * @return list of referencing columns for each foreign key constraint
   */
  val referencingColumns: Map<String, List<ColumnMetaData>>
}
