package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Create placeholder for given columnn. In 99,9% percent of the cases you will just return a plain '?'
 * But there are situation that require special handling, e.g. the PostgreSQL JDBC driver does not allow to set the
 * value of a BIT column directly. Instead you have to usee an explicit cast in your statement, like `CAST(? AS BIT)`.
 *
 * &copy; 2025-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface PreparedStatementPlaceholderFactory {
  /**
   * Return matching columns in target table. Must not be NULL.
   */
  fun map(targetColumn: ColumnMetaData): String
}
