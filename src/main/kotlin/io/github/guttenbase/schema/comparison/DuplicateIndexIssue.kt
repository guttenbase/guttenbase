package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData

/**
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class DuplicateIndexIssue(message: String, indexMetaData: IndexMetaData) :
  IndexIssue(message, SchemaCompatibilityIssueType.DUPLICATE_INDEX, indexMetaData)
