package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData

/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DuplicateForeignKeyIssue(message: String, foreignKeyMetaData: ForeignKeyMetaData) :
  ForeignKeyIssue(message, SchemaCompatibilityIssueType.DUPLICATE_FOREIGN_KEY, foreignKeyMetaData)
