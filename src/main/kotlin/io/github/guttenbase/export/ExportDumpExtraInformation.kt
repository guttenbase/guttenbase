package io.github.guttenbase.export

import io.github.guttenbase.repository.ConnectorRepository
import java.io.Serializable

/**
 * Allow user to add extra informations to the dumped data.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface ExportDumpExtraInformation {
  fun getExtraInformation(
    connectorRepository: ConnectorRepository, connectorId: String, exportDumpConnectionInfo: ExportDumpConnectorInfo
  ): Map<String, Serializable>
}
