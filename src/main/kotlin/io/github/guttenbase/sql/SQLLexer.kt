package io.github.guttenbase.sql

/**
 * Primitive implementation of SQL parser in order to check validity of script.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("SameParameterValue", "KotlinConstantConditions")
class SQLLexer @JvmOverloads constructor(lines: List<String>, private val delimiter: Char = ';') {
  private val sql = lines.joinToString(separator = "\n", transform = { it.trim() })
  private var currentIndex = 0
  private var withinString = false

  fun parse(): List<String> {
    val result = ArrayList<String>()
    val builder = StringBuilder()

    while (hasNext()) {
      when (nextToken()) {
        SQLTokenType.END_OF_LINE, SQLTokenType.WHITESPACE -> {
          read()
          builder.append(' ')
          while (nextToken() == SQLTokenType.WHITESPACE) {
            read()
          }
        }

        SQLTokenType.END_OF_STATEMENT -> {
          read()
          result.add(builder.toString().trim { it <= ' ' })
          builder.setLength(0)
        }

        SQLTokenType.SINGLE_LINE_COMMENT_START -> seekToken(SQLTokenType.END_OF_LINE)
        SQLTokenType.MULTI_LINE_COMMENT_START -> seekToken(SQLTokenType.MULTI_LINE_COMMENT_END)
        SQLTokenType.MULTI_LINE_COMMENT_END -> {
          read()
          read()
        }

        SQLTokenType.ESCAPED_STRING_DELIMITER -> {
          builder.append(read().toChar())
          builder.append(read().toChar())
        }

        SQLTokenType.EOF -> read()
        SQLTokenType.OTHER -> builder.append(read().toChar())
        SQLTokenType.STRING_DELIMITER_START -> {
          builder.append(read().toChar())
          withinString = true
        }

        SQLTokenType.STRING_DELIMITER_END -> {
          builder.append(read().toChar())
          withinString = false
        }
      }
    }

    return result
  }

  private fun seekToken(tokenType: SQLTokenType) {
    var nextToken: SQLTokenType

    do {
      read()
      nextToken = nextToken()
    } while (nextToken != tokenType && nextToken != SQLTokenType.EOF)
  }

  private fun nextToken(): SQLTokenType {
    val ch1 = read()
    val ch2 = read()
    unread(2)

    return if (ch1 < 0) {
      SQLTokenType.EOF
    } else if (!withinString && ch1 == '-'.code && ch2 == '-'.code) {
      SQLTokenType.SINGLE_LINE_COMMENT_START
    } else if (!withinString && ch1 == '/'.code && ch2 == '*'.code) {
      SQLTokenType.MULTI_LINE_COMMENT_START
    } else if (!withinString && ch1 == '*'.code && ch2 == '/'.code) {
      SQLTokenType.MULTI_LINE_COMMENT_END
    } else if (!withinString && ch1 == delimiter.code) {
      SQLTokenType.END_OF_STATEMENT
    } else if (!withinString && ch1 == '\n'.code) {
      SQLTokenType.END_OF_LINE
    } else if (!withinString && ch1 == '\r'.code || ch1 == '\t'.code || ch1 == ' '.code) {
      SQLTokenType.WHITESPACE
    } else if (ch1 == '\''.code && ch2 == '\''.code) {
      SQLTokenType.ESCAPED_STRING_DELIMITER
    } else if (ch1 == '\''.code && ch2 != '\''.code) {
      if (withinString) {
        SQLTokenType.STRING_DELIMITER_END
      } else {
        SQLTokenType.STRING_DELIMITER_START
      }
    } else {
      SQLTokenType.OTHER
    }
  }

  private fun unread(count: Int) {
    currentIndex -= count
  }

  private fun hasNext() = currentIndex < sql.length

  private fun read(): Int {
    val index = currentIndex++

    return if (index >= sql.length) {
      EOF
    } else {
      sql[index].code
    }
  }

  companion object {
    private const val EOF = -1
  }
}
