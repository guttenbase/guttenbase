package io.github.guttenbase.hints.impl


import io.github.guttenbase.hints.ResultSetParametersHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.ResultSetParameters
import java.sql.ResultSet

/**
 * Default result set fetch size is 2000. Result set type is ResultSet.TYPE_FORWARD_ONLY,
 * and concurrency type is ResultSet.CONCUR_READ_ONLY.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultResultSetParametersHint : ResultSetParametersHint() {
 override val value: ResultSetParameters
    get() = DefaultResultSetParameters()

  open class DefaultResultSetParameters : ResultSetParameters {
    override fun getFetchSize(tableMetaData: TableMetaData): Int {
      return 2000
    }

    override fun getResultSetType(tableMetaData: TableMetaData): Int {
      return ResultSet.TYPE_FORWARD_ONLY
    }

    override fun getResultSetConcurrency(tableMetaData: TableMetaData): Int {
      return ResultSet.CONCUR_READ_ONLY
    }
  }
}
