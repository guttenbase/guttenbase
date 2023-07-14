package io.github.guttenbase.export.zip

/**
 * Commonly used constants.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ZipConstants {
  companion object {
    const val DBINFO_NAME = "DB-INFO.txt"
    const val TABLE_INFO_NAME = "TABLE-INFO.txt"
    const val COLUMN_NAME = "COLUMNS"
    const val INDEX_NAME = "INDEXES"
    const val GUTTEN_BASE_NAME = "GuttenBase"
    const val TABLE_DATA_NAME = "DATA"
    const val PATH_SEPARATOR = '/'
    const val METADATA_NAME = "METADATA"
    const val EXTRA_INFO_NAME = "EXTRA-INFO"
    const val PREFIX = GUTTEN_BASE_NAME + PATH_SEPARATOR
    const val META_DATA = PREFIX + METADATA_NAME
    const val EXTRA_INFO = PREFIX + EXTRA_INFO_NAME
    const val META_INF = "META-INF"
    const val MANIFEST_NAME = META_INF + PATH_SEPARATOR + "MANIFEST.MF"
  }
}
