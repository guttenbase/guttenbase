package io.github.guttenbase.tools

import io.github.guttenbase.meta.DatabaseEntityMetaData

/**
 * Set fetch size and result set parameters, i.e.
 * <pre>
 * stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
 * stmt.setFetchSize(100);
</pre> *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ResultSetParameters {
  fun getFetchSize(tableMetaData: DatabaseEntityMetaData): Int
  fun getResultSetType(tableMetaData: DatabaseEntityMetaData): Int
  fun getResultSetConcurrency(tableMetaData: DatabaseEntityMetaData): Int
}
