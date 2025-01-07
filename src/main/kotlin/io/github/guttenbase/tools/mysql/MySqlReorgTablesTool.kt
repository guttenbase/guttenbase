package io.github.guttenbase.tools.mysql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute OPTIMIZE TABLE table;
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class MySqlReorgTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "OPTIMIZE TABLE $TABLE_PLACEHOLDER;")
