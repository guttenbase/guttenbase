package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.NumberOfRowsPerBatchHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.NumberOfRowsPerBatch


/**
 * Disable multiple value clause feature
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DisableMultipleNumberOfRowsPerBatchHint : NumberOfRowsPerBatchHint() {
  override val value: NumberOfRowsPerBatch
    get() = object : NumberOfRowsPerBatch {
      override fun getNumberOfRowsPerBatch(targetTableMetaData: TableMetaData) = 1

      override fun useMultipleValuesClauses(targetTableMetaData: TableMetaData) = false
    }
}
