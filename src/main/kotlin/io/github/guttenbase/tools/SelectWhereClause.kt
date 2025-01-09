package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData

/**
 * Optionally configure the SELECT statement created to read data from source tables with a WHERE clause.
 *
 * &copy; 2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface SelectWhereClause {
  fun getWhereClause(table: TableMetaData): String
}
