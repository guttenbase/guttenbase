package io.github.guttenbase.tools.postgresql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute VACUUM ANALYZE table;
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
open class PostgresqlVacuumTablesTool(connectorRepository: ConnectorRepository) :
  AbstractTablesOperationTool(connectorRepository, "VACUUM ANALYZE $TABLE_PLACEHOLDER;")
