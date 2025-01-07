package io.github.guttenbase.mapping

import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * Select target column(s) for given source column. Usually, this will a 1:1 relationship. However, there may be situations where
 * you want to duplicate or transform data into multiple columns. You also may want to drop columns from the source database.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ColumnMapper {
  /**
   * Return matching columns in target table. Must not be NULL.
   */
  fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapperResult

  /**
   * Map the way column names are used in statements. Usually you won't need that, but sometimes you want to map the names, e.g. to add `name`
   * backticks, in order to escape special characters such as white space.
   */
  fun mapColumnName(source: ColumnMetaData, targetTableMetaData: TableMetaData): String

  data class ColumnMapperResult @JvmOverloads constructor(
    val columns: List<ColumnMetaData>,

    /**
     * If the column cannot be found in the target table this raises an error. However, if you explicitly want to drop that
     * column and this method returns true it will just output a warning instead.
     */
    val isEmptyColumnListOk: Boolean = false
  )
}
