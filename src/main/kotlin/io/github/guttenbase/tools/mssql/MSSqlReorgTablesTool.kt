package io.github.guttenbase.tools.mssql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool
import java.sql.SQLException

/**
 * Will execute OPTIMIZE TABLE table;
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class MSSqlReorgTablesTool(connectorRepository: ConnectorRepository) : AbstractTablesOperationTool(
  connectorRepository, "ALTER INDEX ALL ON " + TABLE_PLACEHOLDER +
      " REBUILD WITH (FILLFACTOR = 80, SORT_IN_TEMPDB = ON, STATISTICS_NORECOMPUTE = ON"
) {
  @Throws(SQLException::class)
  fun executeOnAllTables(target: String) {
    executeOnAllTables(target, false, false)
  }
}
