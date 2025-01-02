package io.github.guttenbase.statements

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
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
   * Retrieve data in some deterministic order
   */
  override fun createOrderBy(tableMetaData: TableMetaData): String {
    val columns = tableMetaData.columnMetaData.filter { it.columnType.comparable() }
      .sortedWith { column1, column2 ->
        when {
          column1.isPrimaryKey -> -1
          column2.isPrimaryKey -> 1
          else -> column1.compareTo(column2)
        }
      }
      .map {
        val rawColumnName = columnMapper.mapColumnName(it, tableMetaData)
        tableMetaData.databaseType.escapeDatabaseEntity(rawColumnName)
      }

    return if (columns.isEmpty()) "" else "ORDER BY " + columns.joinToString()
  }
}

/**
 * BLOB or the like are not applicable for ordering
 */
private fun Int.comparable(): Boolean = when (this) {
  in Types.BIT..Types.BIGINT -> true
  in Types.CHAR..Types.SMALLINT -> true
  in Types.DATE..Types.TIMESTAMP -> true
  Types.LONGVARCHAR, Types.NCHAR, Types.VARCHAR, Types.NVARCHAR, Types.LONGNVARCHAR -> true
  Types.BOOLEAN -> true
  else -> false
}