package io.github.guttenbase.tools.derby

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE system procedure
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DerbyCompressTablesTool(connectorRepository: ConnectorRepository) : AbstractTablesOperationTool(
  connectorRepository,
  "CALL SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE ('sa', '$TABLE_PLACEHOLDER', 1, 1, 1);"
)
