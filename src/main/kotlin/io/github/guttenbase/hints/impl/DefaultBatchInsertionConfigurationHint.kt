package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.BatchInsertionConfigurationHint
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.tools.BatchInsertionConfiguration

/**
 * Default number of VALUES clauses is 2000, default number of data items is 30000.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultBatchInsertionConfigurationHint : BatchInsertionConfigurationHint() {
  override val value: BatchInsertionConfiguration
    get() = DefaultBatchInsertionConfiguration()
}

open class DefaultBatchInsertionConfiguration : BatchInsertionConfiguration {
  override fun getNumberOfRowsPerBatch(targetTable: TableMetaData) = 2000

  override fun getMaxNumberOfDataItems(targetTable: TableMetaData) = when (targetTable.databaseType) {
    DatabaseType.MSSQL -> 2000
    else -> 30000
  }
}