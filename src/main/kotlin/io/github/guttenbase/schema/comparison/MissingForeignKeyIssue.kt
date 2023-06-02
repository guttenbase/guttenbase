package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData


class MissingForeignKeyIssue(message: String, foreignKeyMetaData: ForeignKeyMetaData) :
  ForeignKeyIssue(message, SchemaCompatibilityIssueType.MISSING_FOREIGN_KEY, foreignKeyMetaData)
