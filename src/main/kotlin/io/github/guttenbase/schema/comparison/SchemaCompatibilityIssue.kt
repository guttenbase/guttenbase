package io.github.guttenbase.schema.comparison

/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class SchemaCompatibilityIssue(val message: String) {
  abstract val compatibilityIssueType: SchemaCompatibilityIssueType

  override fun toString() = "$compatibilityIssueType:$message"
}
