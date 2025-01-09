package io.github.guttenbase.tools.mssql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool

/**
 * Will execute OPTIMIZE TABLE table;
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
class MSSqlReorgTablesTool(connectorRepository: ConnectorRepository) : AbstractTablesOperationTool(
  connectorRepository, "ALTER INDEX ALL ON " + TABLE_PLACEHOLDER +
      " REBUILD WITH (FILLFACTOR = 80, SORT_IN_TEMPDB = ON, STATISTICS_NORECOMPUTE = ON"
) {
  fun executeOnAllTables(target: String) {
    executeOnAllTables(target, false, false)
  }
}
