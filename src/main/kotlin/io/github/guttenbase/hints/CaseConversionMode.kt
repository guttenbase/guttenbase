package io.github.guttenbase.hints

/**
 * Used to map table names, column names, etc.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
enum class CaseConversionMode {
  NONE,
  UPPER,
  LOWER;

  fun convert(name: String): String {
    return when (this) {
      LOWER -> name.lowercase()
      UPPER -> name.uppercase()
      NONE -> name
    }
  }
}
