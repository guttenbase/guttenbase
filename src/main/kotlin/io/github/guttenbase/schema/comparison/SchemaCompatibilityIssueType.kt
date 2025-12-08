package io.github.guttenbase.schema.comparison

import java.util.logging.Level

/**
 * Type of compatibility issue
 *
 * &copy; 2012-2044 tech@spree
 *
 */
enum class SchemaCompatibilityIssueType(val severity: Level) {
  MISSING_TABLE(Level.SEVERE),
  MISSING_COLUMN(Level.SEVERE),
  INCOMPATIBLE_COLUMNS(Level.SEVERE),
  DROPPED_COLUMN(Level.WARNING),
  ADDITIONAL_NONNULL_COLUMN(Level.SEVERE),
  ADDITIONAL_COLUMN(Level.WARNING),
  MISSING_INDEX(Level.INFO),
  DUPLICATE_INDEX(Level.WARNING),
  DUPLICATE_FOREIGN_KEY(Level.WARNING),
  MISSING_FOREIGN_KEY(Level.WARNING)
}
