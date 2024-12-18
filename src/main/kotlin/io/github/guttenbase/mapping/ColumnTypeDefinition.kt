package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.PRECISION_PLACEHOLDER

/**
 * Definition of column type as used in target DB
 *
 *  &copy; 2012-2034 akquinet tech@spree
 */
data class ColumnTypeDefinition(
  val sourceColumn: ColumnMetaData, val typeName: String,
  val precision: Int = 0, val scale: Int = 0, val usePrecision: Boolean = false
) {
  val precisionClause: String
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