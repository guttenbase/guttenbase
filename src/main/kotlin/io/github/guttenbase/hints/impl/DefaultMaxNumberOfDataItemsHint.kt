package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.MaxNumberOfDataItemsHint
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.tools.MaxNumberOfDataItems


/**
 * Default maximum is 30000.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultMaxNumberOfDataItemsHint : MaxNumberOfDataItemsHint() {
  override val value: MaxNumberOfDataItems
    get() = MaxNumberOfDataItems { targetTableMetaData ->
      when (targetTableMetaData.databaseType) {
        DatabaseType.MSSQL -> 2000
        else -> 30000
      }
    }
}
