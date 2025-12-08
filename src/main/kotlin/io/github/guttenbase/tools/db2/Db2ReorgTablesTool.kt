package io.github.guttenbase.tools.db2

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute REORG TABLE table;
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
open class Db2ReorgTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "CALL SYSPROC.ADMIN_CMD('REORG TABLE $TABLE_PLACEHOLDER');")
