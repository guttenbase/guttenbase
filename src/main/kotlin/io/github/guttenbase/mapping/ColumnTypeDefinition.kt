package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.PRECISION_PLACEHOLDER
import io.github.guttenbase.utils.Util.mormalizeNegativeScale
import java.sql.JDBCType
import java.sql.JDBCType.ARRAY

/**
 * Resolve column type definition for given column and database type.
 */
fun interface ColumnTypeDefinitionResolver {
  fun resolve(type: ColumnTypeDefinition): ColumnTypeDefinition?
}

/**
 * Definition of column type as used in target DB
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
data class ColumnTypeDefinition @JvmOverloads constructor(
  val sourceColumn: ColumnMetaData,
  val targetDatabase: DatabaseMetaData,
  val typeName: String,
  val jdbcType: JDBCType = sourceColumn.jdbcColumnType,
  val usePrecisionClause: Boolean = false,
  val precision: Int = sourceColumn.precision,
  // https://forums.oracle.com/ords/apexds/post/number-data-type-s-negative-scale-4474
  val scale: Int = if (sourceColumn.scale < 0) sourceColumn.scale.mormalizeNegativeScale() else sourceColumn.scale
) {
  val sourceDatabase get() = sourceColumn.container.database
  val databaseType = targetDatabase.databaseType
  val usePrecision = (usePrecisionClause && precision > 0) || typeName.contains(PRECISION_PLACEHOLDER)

  constructor(type: ColumnTypeDefinition, typeName: String, jdbcType: JDBCType = type.sourceColumn.jdbcColumnType)
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

    return if (sourceColumn.jdbcColumnType == ARRAY) {
      databaseType.arrayType(typeName)
    } else {
      typeName
    }.replace(PRECISION_PLACEHOLDER, precisionClause)
  }
}