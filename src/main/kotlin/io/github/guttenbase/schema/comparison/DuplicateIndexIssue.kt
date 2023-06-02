package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData

class DuplicateIndexIssue(message: String, indexMetaData: IndexMetaData) :
  IndexIssue(message, SchemaCompatibilityIssueType.DUPLICATE_INDEX, indexMetaData)
