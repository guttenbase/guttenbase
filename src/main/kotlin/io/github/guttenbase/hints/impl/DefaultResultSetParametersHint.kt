package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.ResultSetParametersHint
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.tools.ResultSetParameters
import java.sql.ResultSet

/**
 * Default result set fetch size is 2000. Result set type is ResultSet.TYPE_FORWARD_ONLY,
 * and concurrency type is ResultSet.CONCUR_READ_ONLY.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultResultSetParametersHint : ResultSetParametersHint() {
  override val value: ResultSetParameters
    get() = object : ResultSetParameters {
      override fun getFetchSize(tableMetaData: DatabaseEntityMetaData): Int {
        return 2000
      }

      override fun getResultSetType(tableMetaData: DatabaseEntityMetaData): Int {
        return ResultSet.TYPE_FORWARD_ONLY
      }

      override fun getResultSetConcurrency(tableMetaData: DatabaseEntityMetaData): Int {
        return ResultSet.CONCUR_READ_ONLY
      }
    }
}
