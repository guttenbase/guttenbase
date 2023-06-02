package io.github.guttenbase.schema.comparison

abstract class SchemaCompatibilityIssue(val message: String) {
  abstract val compatibilityIssueType: SchemaCompatibilityIssueType

  override fun toString() = "$compatibilityIssueType:$message"
}
