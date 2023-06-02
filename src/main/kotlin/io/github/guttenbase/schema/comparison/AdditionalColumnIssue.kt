package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData

class AdditionalColumnIssue(message: String, val sourceColumn: ColumnMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.ADDITIONAL_COLUMN
}
