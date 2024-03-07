package io.github.guttenbase.statements

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Types

/**
 * Create SELECT statement for copying data.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class SelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  /**
   * Try to retrieve data in some deterministic order
   */
  override fun createOrderBy(tableMetaData: TableMetaData): String {
    val buf = StringBuilder("ORDER BY ")
    var columnsAdded = 0

    for (i in 0 until tableMetaData.columnCount) {
      val columnMetaData = tableMetaData.columnMetaData[i]
      val columnName = columnMapper.mapColumnName(columnMetaData, tableMetaData)
      val jdbcType = columnMetaData.columnType

      if (jdbcType.comparable()) {
        buf.append(columnName).append(", ")
        columnsAdded++
      }
    }

    return if (columnsAdded > 0) {
      buf.setLength(buf.length - 2)
      buf.toString()
    } else {
      ""
    }
  }
}

/** No BLOB or the like for ordering
 */
private fun Int.comparable(): Boolean = when (this) {
  in Types.BIT..Types.BIGINT -> true
  in Types.CHAR..Types.SMALLINT -> true
  in Types.DATE..Types.TIMESTAMP -> true
  Types.LONGVARCHAR, Types.NCHAR, Types.VARCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> true
  Types.BOOLEAN -> true
  else -> false
}