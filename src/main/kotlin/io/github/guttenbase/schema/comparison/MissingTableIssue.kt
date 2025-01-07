package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.TableMetaData


/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class MissingTableIssue(message: String, val sourceTableMetaData: TableMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.MISSING_TABLE
}
