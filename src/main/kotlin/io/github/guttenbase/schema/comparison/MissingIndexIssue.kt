package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData


/**
 * &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class MissingIndexIssue(message: String, indexMetaData: IndexMetaData) :
  IndexIssue(message, SchemaCompatibilityIssueType.MISSING_INDEX, indexMetaData)
