package io.github.guttenbase.tools.postgresql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute VACUUM ANALYZE table;
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class PostgresqlVacuumTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "VACUUM ANALYZE $TABLE_PLACEHOLDER;")
