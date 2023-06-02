package io.github.guttenbase.tools.mysql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute OPTIMIZE TABLE table;
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class MySqlReorgTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "OPTIMIZE TABLE $TABLE_PLACEHOLDER;")
