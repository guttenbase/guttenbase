package io.github.guttenbase.schema.comparison

import io.github.guttenbase.meta.ColumnMetaData


/**
 * &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DroppedColumnIssue(message: String, val sourceColumn: ColumnMetaData) : SchemaCompatibilityIssue(message) {
  override val compatibilityIssueType: SchemaCompatibilityIssueType
    get() = SchemaCompatibilityIssueType.DROPPED_COLUMN
}
