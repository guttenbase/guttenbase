package io.github.guttenbase.schema.comparison

import java.util.logging.Level

/**
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class SchemaCompatibilityIssues {
  val compatibilityIssues = ArrayList<SchemaCompatibilityIssue>()

  fun addIssue(issue: SchemaCompatibilityIssue) {
    this.compatibilityIssues.add(issue)
  }

  val isSevere: Boolean
    get() = compatibilityIssues.any { Level.SEVERE == it.compatibilityIssueType.severity }

  fun contains(issueType: SchemaCompatibilityIssueType): SchemaCompatibilityIssue? =
    compatibilityIssues.firstOrNull { issueType == it.compatibilityIssueType }

  override fun toString() = compatibilityIssues.joinToString(separator = "\n", transform = {it.toString()})
}
