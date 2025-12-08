package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData


/**
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class MissingForeignKeyIssue(message: String, foreignKeyMetaData: ForeignKeyMetaData) :
  ForeignKeyIssue(message, SchemaCompatibilityIssueType.MISSING_FOREIGN_KEY, foreignKeyMetaData)
