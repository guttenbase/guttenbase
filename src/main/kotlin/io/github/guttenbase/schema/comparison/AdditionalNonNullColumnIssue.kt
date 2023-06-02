package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


class AdditionalNonNullColumnIssue(message: String, val sourceColumn: ColumnMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.ADDITIONAL_NONNULL_COLUMN
}
