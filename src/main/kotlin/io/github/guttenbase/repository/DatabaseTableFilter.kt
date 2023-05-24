package io.github.guttenbase.repository

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

/**
 * Regard which tables when @see [DatabaseMetaDataInspectorTool] is inquiring the database for tables. The methods refer to
 * the parameters passed to JDBC data base meta data methods such as
 * [DatabaseMetaData.getTableMetaData]
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseTableFilter {
    fun getCatalog(databaseMetaData: DatabaseMetaData): String
    fun getSchema(databaseMetaData: DatabaseMetaData): String
    fun getSchemaPattern(databaseMetaData: DatabaseMetaData): String
    fun getTableNamePattern(databaseMetaData: DatabaseMetaData): String
    fun getTableTypes(databaseMetaData: DatabaseMetaData): Array<String>

    /**
     * Additionally you may add checks to the resulting meta data object
     *
     * @return true if the table should be added the database meta data
     */
    fun accept(table: TableMetaData): Boolean
}
