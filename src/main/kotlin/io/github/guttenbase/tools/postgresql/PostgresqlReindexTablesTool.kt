package io.github.guttenbase.tools.postgresql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute REINDEX TABLE table;
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class PostgresqlReindexTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "REINDEX TABLE $TABLE_PLACEHOLDER;")
