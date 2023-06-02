package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


class MissingColumnIssue(message: String, val sourceColumn: ColumnMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.MISSING_COLUMN
}
