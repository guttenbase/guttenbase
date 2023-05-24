package io.github.guttenbase.meta

import java.util.*

/**
 * Extension for internal access.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface InternalColumnMetaData : ColumnMetaData {
    /**
     * Make columns globally uniq since the name may not be uniq within the data base.
     */
    val columnId: UUID
    fun setPrimaryKey(primaryKey: Boolean)
}