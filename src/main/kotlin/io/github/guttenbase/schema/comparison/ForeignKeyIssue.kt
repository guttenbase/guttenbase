package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData

abstract class ForeignKeyIssue(
  message: String,
  override val compatibilityIssueType: SchemaCompatibilityIssueType,
  val foreignKeyMetaData: ForeignKeyMetaData
) : SchemaCompatibilityIssue(message)
