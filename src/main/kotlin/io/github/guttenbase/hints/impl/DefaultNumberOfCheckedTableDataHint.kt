package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.NumberOfCheckedTableDataHint
import io.github.guttenbase.tools.NumberOfCheckedTableData


/**
 * Default number of checked rows is 100.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultNumberOfCheckedTableDataHint : NumberOfCheckedTableDataHint() {
 override val value: NumberOfCheckedTableData
    get() = object : NumberOfCheckedTableData {
      override val numberOfCheckedTableData: Int
        get() = 100
    }
}
