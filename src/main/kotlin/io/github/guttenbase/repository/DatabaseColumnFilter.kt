package io.github.guttenbase.repository

import io.github.guttenbase.meta.ColumnMetaData

/**
 * Regard which columns when @see [DatabaseMetaDataInspectorTool] is inquiring the database for columns
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseColumnFilter {
    /**
     * Perform custom check on column before adding it to table meta data
     */
    fun accept(columnMetaData: ColumnMetaData): Boolean
}
