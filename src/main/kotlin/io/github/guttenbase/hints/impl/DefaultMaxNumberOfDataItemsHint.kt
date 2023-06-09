package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.MaxNumberOfDataItemsHint
import io.github.guttenbase.tools.MaxNumberOfDataItems


/**
 * Default maximum is 30000.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultMaxNumberOfDataItemsHint : MaxNumberOfDataItemsHint() {
 override val value: MaxNumberOfDataItems
    get() = MaxNumberOfDataItems { 30000 }
}
