package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


class DroppedColumnIssue(message: String, val sourceColumn: ColumnMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.DROPPED_COLUMN
}
