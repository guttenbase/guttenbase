package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.NumberOfRowsPerBatchHint
import io.github.guttenbase.tools.NumberOfRowsPerBatch


/**
 * Default number of VALUES clauses is 2000.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultNumberOfRowsPerBatchHint : NumberOfRowsPerBatchHint() {
 override val value: NumberOfRowsPerBatch
    get() = NumberOfRowsPerBatch { 2000 }
}
