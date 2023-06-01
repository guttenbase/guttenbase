package io.github.guttenbase.hints.impl

import io.github.guttenbase.defaults.impl.DefaultSplitColumn
import io.github.guttenbase.hints.SplitColumnHint
import io.github.guttenbase.tools.SplitColumn


/**
 * Sometimes the amount of data exceeds buffers. In these cases we need to split the data by some given range, usually the primary key.
 * I.e., the data is read in chunks where these chunks are split using the ID column range of values.
 *
 *
 * By default use the first primary key column, if any. Otherwise returns the first column.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultSplitColumnHint : SplitColumnHint() {
 override val value: SplitColumn
    get() = DefaultSplitColumn()
}
