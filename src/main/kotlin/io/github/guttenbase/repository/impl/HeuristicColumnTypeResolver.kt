package io.github.guttenbase.repository.impl

import io.github.guttenbase.mapping.ColumnTypeResolver
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.databaseType

/**
 * Will check column type names and determine what Java type is appropriate.
 *
 * Mapping is based on heuristic knowledge and experience.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object HeuristicColumnTypeResolver : ColumnTypeResolver {
  /**
   * Performs some heuristic checks on given column type.
   */
  override fun getColumnType(columnMetaData: ColumnMetaData): ColumnType {
    val columnType = columnMetaData.columnTypeName.uppercase()
    val databaseType = columnMetaData.databaseType

    return checkDatabaseSpecificTypes(columnType, databaseType)
      ?: return when {
        columnType.endsWith("CHAR") || columnType.endsWith("TEXT") || columnType.startsWith("CHAR")
            || columnType.startsWith("VARCHAR") -> ColumnType.CLASS_STRING

        "BIGINT" == columnType -> ColumnType.CLASS_LONG

        columnType.startsWith("NUMERIC") || "DECIMAL" == columnType -> ColumnType.CLASS_BIGDECIMAL

        "INT2" == columnType || "SMALLINT" == columnType -> ColumnType.CLASS_SHORT

        columnType.startsWith("INT") || columnType.endsWith("INT") || columnType == "COUNTER" -> ColumnType.CLASS_INTEGER

        columnType.endsWith("BLOB") || columnType == "IMAGE" -> ColumnType.CLASS_BLOB

        columnType == "BIT" || columnType.startsWith("BOOL") -> ColumnType.CLASS_BOOLEAN

        columnType == "BYTEA" || columnType.startsWith("VARBINARY") -> ColumnType.CLASS_BLOB

        columnType == "DATETIME" -> ColumnType.CLASS_TIMESTAMP

        columnType.startsWith("TIMESTAMP") -> ColumnType.CLASS_TIMESTAMP

        else -> ColumnType.valueOfClassName(columnMetaData.columnClassName)
      }
  }

  private fun checkDatabaseSpecificTypes(columnType: String, databaseType: DatabaseType): ColumnType? =
    when (databaseType) {
      POSTGRESQL -> when (columnType) {
        "BIT" -> ColumnType.CLASS_STRING
        "INT8" -> ColumnType.CLASS_BIGDECIMAL
        "OID" -> ColumnType.CLASS_BLOB
        "BYTEA" -> ColumnType.CLASS_BYTES
        else -> null
      }

      ORACLE -> when (columnType) {
        "CLOB" -> ColumnType.CLASS_STRING
        "TIMESTAMP" -> ColumnType.CLASS_TIMESTAMP
        "XMLTYPE" -> ColumnType.CLASS_SQLXML
        else -> null
      }

      H2DB -> when (columnType) {
        "CLOB" -> ColumnType.CLASS_STRING
        else -> null
      }

      else -> null
    }
}
