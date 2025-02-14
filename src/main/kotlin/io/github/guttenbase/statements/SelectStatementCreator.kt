package io.github.guttenbase.statements

import io.github.guttenbase.defaults.impl.DefaultColumnComparator
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Types

/**
 * Create SELECT statement for copying data.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class SelectStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractSelectStatementCreator(connectorRepository, connectorId) {
  /**
   * Retrieve data in some deterministic order
   */
  override fun createOrderBy(table: TableMetaData): String {
    val columns = table.columns.filter { it.columnType.comparable() }
      .sortedWith(DefaultColumnComparator).map {
        val rawColumnName = columnMapper.mapColumnName(it, table)
        table.databaseType.escapeDatabaseEntity(rawColumnName)
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