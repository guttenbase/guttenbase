package io.github.guttenbase.tools.derby

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractTablesOperationTool


/**
 * Will execute SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE system procedure
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DerbyCompressTablesTool(connectorRepository: ConnectorRepository) : AbstractTablesOperationTool(
  connectorRepository,
  "CALL SYSCS_UTIL.SYSCS_INPLACE_COMPRESS_TABLE ('sa', '$TABLE_PLACEHOLDER', 1, 1, 1);"
)
