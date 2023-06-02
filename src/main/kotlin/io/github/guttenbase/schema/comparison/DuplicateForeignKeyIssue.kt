package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData

class DuplicateForeignKeyIssue(message: String, foreignKeyMetaData: ForeignKeyMetaData) :
  ForeignKeyIssue(message, SchemaCompatibilityIssueType.DUPLICATE_FOREIGN_KEY, foreignKeyMetaData)
