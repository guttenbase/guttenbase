package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData


/**
 * How many data items may the INSERT statement have in total. I.e., how many '?' placeholders does the database support in a single
 * statement.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface MaxNumberOfDataItems {
  fun getMaxNumberOfDataItems(targetTableMetaData: TableMetaData): Int
}
