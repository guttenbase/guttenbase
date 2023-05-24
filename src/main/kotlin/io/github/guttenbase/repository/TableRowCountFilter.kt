package io.github.guttenbase.repository

import io.github.guttenbase.meta.TableMetaData

/**
 * Some tables are really big and computing the row count may take too long for the data base.
 *
 *
 * Using this hint the @see [DatabaseMetaDataInspectorTool] will compute the row count only
 * for the given tables.
 */
interface TableRowCountFilter {
    fun accept(tableMetaData: TableMetaData): Boolean
    fun defaultRowCount(tableMetaData: TableMetaData): Int
}
