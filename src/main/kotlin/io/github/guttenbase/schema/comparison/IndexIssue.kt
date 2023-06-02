package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData


abstract class IndexIssue(
  message: String,
  override val compatibilityIssueType: SchemaCompatibilityIssueType,
  val indexMetaData: IndexMetaData
) : SchemaCompatibilityIssue(message)

