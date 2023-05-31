package io.github.guttenbase.sql

/**
 * Tokens for [SQLLexer].
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
data class SQLToken(val sqlTokenType: SQLTokenType, val token: String) {
  override fun toString() = sqlTokenType.name
}
