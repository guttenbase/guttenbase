@file:Suppress("unused")

package io.github.guttenbase.meta

import java.io.Serializable
import java.sql.JDBCType
import java.sql.JDBCType.*

/**
 * Information about a table column.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ColumnMetaData : Comparable<ColumnMetaData>, Serializable {
  /**
   * Column type as defined in [java.sql.Types]
   */
  val columnType: Int
  val jdbcColumnType: JDBCType
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
  val isGenerated: Boolean
  val columnSize: Int
  val defaultValue: String?

  /**
   * @return referenced columns for each foreign key constraint
   */
  val referencedColumns: Map<String, List<ColumnMetaData>>

  /**
   * @return list of referencing columns for each foreign key constraint
   */
  val referencingColumns: Map<String, List<ColumnMetaData>>
}

val STRING_TYPES = listOf(CHAR, NCHAR, LONGVARCHAR, LONGNVARCHAR, VARCHAR, NVARCHAR)
val INTEGER_TYPES = listOf(SMALLINT, INTEGER)
val REAL_TYPES = listOf(DOUBLE, FLOAT)
val CLOB_TYPES = listOf(CLOB, NCLOB, LONGVARCHAR, LONGNVARCHAR)
val BLOB_TYPES = listOf(BLOB)
val OBJECT_TYPES = listOf(JAVA_OBJECT)
val BINARY_TYPES = listOf(LONGVARBINARY, VARBINARY, BINARY)
val BOOLEAN_TYPES = listOf(BOOLEAN, BIT)
val NUMERIC_TYPES = listOf(NUMERIC, DECIMAL, REAL)
val DATE_TYPES = listOf(TIME, TIMESTAMP, DATE)
val OTHER_TYPES = listOf(OTHER, JAVA_OBJECT)

fun JDBCType.isClobType() = this in CLOB_TYPES
fun JDBCType.isIntegerType() = this in INTEGER_TYPES
fun JDBCType.isRealType() = this in REAL_TYPES
fun JDBCType.isStringType() = this in STRING_TYPES
fun JDBCType.isBlobType() = this in BLOB_TYPES
fun JDBCType.isBinaryType() = this in BINARY_TYPES
fun JDBCType.isNumericType() = this in NUMERIC_TYPES
fun JDBCType.isDateType() = this in DATE_TYPES
fun JDBCType.isOtherType() = this in OTHER_TYPES
fun Int.hasJDBCType(): Boolean = JDBCType.entries.any { it.vendorTypeNumber.toInt() == this.toInt() }

val STANDARD_TYPES = JDBCType.entries.map { it.name }.toSet()

val ColumnMetaData.databaseType get() = tableMetaData.databaseType
val ColumnMetaData.connectorId get() = tableMetaData.connectorId
val ColumnMetaData.connectorRepository get() = tableMetaData.connectorRepository