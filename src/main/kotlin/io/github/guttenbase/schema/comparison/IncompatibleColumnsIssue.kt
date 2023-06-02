package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


class IncompatibleColumnsIssue(message: String, val sourceColumn: ColumnMetaData, val targetColumn: ColumnMetaData) :
  SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.INCOMPATIBLE_COLUMNS
}
