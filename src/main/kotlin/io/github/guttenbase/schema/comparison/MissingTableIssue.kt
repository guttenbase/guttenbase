package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.TableMetaData


class MissingTableIssue(message: String, val sourceTableMetaData: TableMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.MISSING_TABLE
}
