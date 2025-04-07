package io.github.guttenbase.meta

import io.github.guttenbase.repository.ConnectorRepository
import java.sql.JDBCType

/**
 * Extension for internal access.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
internal interface InternalDatabaseMetaData : DatabaseMetaData {
  override var connectorRepository: ConnectorRepository
  override var connectorId: String

  fun addTable(tableMetaData: TableMetaData)
  fun addView(viewMetaData: ViewMetaData)
  fun removeTable(tableMetaData: TableMetaData)
  fun removeView(viewMetaData: ViewMetaData)
  fun addSupportedType(type: String, jdbcType: JDBCType, precision: Int, scale: Int, nullable: Boolean)
}