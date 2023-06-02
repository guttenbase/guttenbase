package io.github.guttenbase.tools.db2

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute REORG INDEXES ALL FOR TABLE table;
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class Db2ReorgIndexesTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "CALL SYSPROC.ADMIN_CMD('REORG INDEXES ALL FOR TABLE $TABLE_PLACEHOLDER');") {
  override fun isApplicableOnTable(tableMetaData: TableMetaData): Boolean {
    // Prevent DB2 SQL Error: SQLCODE=-1146, SQLSTATE=01H52
    return tableMetaData.indexes.isNotEmpty()
  }
}
