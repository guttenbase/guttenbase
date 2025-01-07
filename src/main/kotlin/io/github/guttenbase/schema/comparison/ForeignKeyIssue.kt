package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ForeignKeyMetaData

/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
abstract class ForeignKeyIssue(
  message: String,
  override val compatibilityIssueType: SchemaCompatibilityIssueType,
  val foreignKeyMetaData: ForeignKeyMetaData
) : SchemaCompatibilityIssue(message)
