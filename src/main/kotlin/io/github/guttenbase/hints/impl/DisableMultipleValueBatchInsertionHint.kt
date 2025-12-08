package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.BatchInsertionConfigurationHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.BatchInsertionConfiguration

/**
 * Disable multiple value clause feature at all
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DisableMultipleValueBatchInsertionHint : BatchInsertionConfigurationHint() {
  override val value: BatchInsertionConfiguration
    get() = object : DefaultBatchInsertionConfiguration() {
      override fun getNumberOfRowsPerBatch(targetTable: TableMetaData) = 1

      override fun useMultipleValuesClauses(targetTable: TableMetaData) = false
    }
}
