package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.PRECISION_PLACEHOLDER
import java.sql.JDBCType

/**
 * Resolve column type definition for given column and database type.
 */
fun interface ColumnTypeDefinitionResolver {
  fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition?
}

/**
 * Definition of column type as used in target DB
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
data class ColumnTypeDefinition @JvmOverloads constructor(
  val sourceColumn: ColumnMetaData,
  val targetDatabase: DatabaseMetaData,
  val typeName: String,
  val jdbcType: JDBCType = sourceColumn.jdbcColumnType,
  val usePrecisionClause: Boolean = false,
  val precision: Int = sourceColumn.precision,
  val scale: Int = if (sourceColumn.scale < 0) 8 else sourceColumn.scale // https://forums.oracle.com/ords/apexds/post/number-data-type-s-negative-scale-4474
) {
  val sourceDatabase get() = sourceColumn.tableMetaData.databaseMetaData
  val databaseType = targetDatabase.databaseType
  val usePrecision = (usePrecisionClause && precision > 0) || typeName.contains(PRECISION_PLACEHOLDER)

  constructor(type: ColumnTypeDefinition, typeName: String, jdbcType: JDBCType)
      : this(type.sourceColumn, type.targetDatabase, typeName, jdbcType, type.usePrecisionClause, type.precision, type.scale)

  private val precisionClause: String
    get() {
      val precisionClause = StringBuilder()

      if (usePrecision) {
        precisionClause.append("($precision")

        if (scale > 0) {
          precisionClause.append(", $scale")
        }

        precisionClause.append(")")
      }

      return precisionClause.toString()
    }

  override fun toString(): String {
    val typeName = if (!typeName.contains(PRECISION_PLACEHOLDER) && usePrecision) {
      typeName + PRECISION_PLACEHOLDER
    } else {
      typeName
    }

    return typeName.replace(PRECISION_PLACEHOLDER, precisionClause)
  }
}