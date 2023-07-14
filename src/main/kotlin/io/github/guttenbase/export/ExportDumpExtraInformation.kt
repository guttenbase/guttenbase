package io.github.guttenbase.export

import io.github.guttenbase.repository.ConnectorRepository
import java.io.Serializable
import java.sql.SQLException

/**
 * Give the user a possibility to add extra informations to the dumped data.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface ExportDumpExtraInformation {
  @Throws(SQLException::class)
  fun getExtraInformation(
    connectorRepository: ConnectorRepository, connectorId: String, exportDumpConnectionInfo: ExportDumpConnectorInfo
  ): Map<String, Serializable>
}
