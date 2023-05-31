package io.github.guttenbase.repository.export

import io.github.guttenbase.repository.ConnectorRepository
import java.io.Serializable
import java.sql.SQLException

/**
 * Give the user a possibility to add extra informations to the dumped data.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ExportDumpExtraInformation {
  @Throws(SQLException::class)
  fun getExtraInformation(
    connectorRepository: ConnectorRepository, connectorId: String, exportDumpConnectionInfo: ExportDumpConnectorInfo
  ): Map<String, Serializable>
}
