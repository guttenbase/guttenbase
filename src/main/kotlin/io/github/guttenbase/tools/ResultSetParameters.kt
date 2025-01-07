package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData

/**
 * Set fetch size and result set parameters, i.e.
 * <pre>
 * stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 * stmt.setFetchSize(100);
</pre> *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * @see MaxNumberOfDataItems
 */
interface ResultSetParameters {
  fun getFetchSize(tableMetaData: TableMetaData): Int
  fun getResultSetType(tableMetaData: TableMetaData): Int
  fun getResultSetConcurrency(tableMetaData: TableMetaData): Int
}
