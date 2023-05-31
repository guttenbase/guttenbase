package io.github.guttenbase.statements

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Types


/**
 * Create SELECT statement for copying data.
 *
 *
 *  2012-2034 akquinet tech@spree
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

    // No BLOB or the like for ordering
    val isOracleOrMssql = (DatabaseType.ORACLE == tableMetaData.databaseMetaData.databaseType
        || DatabaseType.MSSQL == tableMetaData.databaseMetaData.databaseType)
    val rangeFrom = if (isOracleOrMssql) Types.NULL else Types.LONGNVARCHAR // Doesn't like LONG e.g.
    val rangeTo = Types.JAVA_OBJECT

    for (i in 0 until tableMetaData.columnCount) {
      val columnMetaData = tableMetaData.columnMetaData[i]
      val columnName = columnMapper.mapColumnName(columnMetaData, tableMetaData)
      val jdbcType = columnMetaData.columnType

      if (jdbcType in (rangeFrom + 1) until rangeTo) {
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
