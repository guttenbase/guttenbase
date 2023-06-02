package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData


class MissingIndexIssue(message: String, indexMetaData: IndexMetaData) :
  IndexIssue(message, SchemaCompatibilityIssueType.MISSING_INDEX, indexMetaData)
