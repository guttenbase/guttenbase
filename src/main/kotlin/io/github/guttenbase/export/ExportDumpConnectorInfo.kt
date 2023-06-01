package io.github.guttenbase.export

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository

/**
 * Connection info for exporting data to a file.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
data class ExportDumpConnectorInfo(val sourceConnectorId: String, val path: String) : ConnectorInfo {
  override val user: String get() = "user"
  override val password: String get() = "password"
  override val schema: String get() = "schema"
  override val databaseType: DatabaseType get() = DatabaseType.EXPORT_DUMP

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String) =
    ExportDumpConnector(connectorRepository, connectorId, this)

  companion object {
    private const val serialVersionUID = 1L
  }
}
