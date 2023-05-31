package io.github.guttenbase.sql

/**
 * Tokens for [SQLLexer].
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
enum class SQLTokenType {
  WHITESPACE,
  SINGLE_LINE_COMMENT_START,
  MULTI_LINE_COMMENT_START,
  MULTI_LINE_COMMENT_END,
  STRING_DELIMITER_START,
  STRING_DELIMITER_END,
  END_OF_STATEMENT,
  END_OF_LINE,
  OTHER,
  EOF,
  ESCAPED_STRING_DELIMITER
}
