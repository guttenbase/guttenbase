package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.IndexMetaData


/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class IndexIssue(
  message: String,
  override val compatibilityIssueType: SchemaCompatibilityIssueType,
  val indexMetaData: IndexMetaData
) : SchemaCompatibilityIssue(message)

