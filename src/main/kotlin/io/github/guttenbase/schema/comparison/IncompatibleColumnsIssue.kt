package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
class IncompatibleColumnsIssue(message: String, val sourceColumn: ColumnMetaData, val targetColumn: ColumnMetaData) :
  SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.INCOMPATIBLE_COLUMNS
}
