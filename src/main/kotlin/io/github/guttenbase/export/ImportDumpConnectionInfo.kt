package io.github.guttenbase.export

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.net.URL

/**
 * Connection info for importing data from a file.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
data class ImportDumpConnectionInfo(val path: URL) : ConnectorInfo {
  override val user: String get() = "user"
  override val password: String get() = "password"
  override val schema: String get() = "schema"
  override val databaseType: DatabaseType get() = DatabaseType.IMPORT_DUMP

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String) =
    ImportDumpConnector(connectorRepository, connectorId, this)

  companion object {
    private const val serialVersionUID = 1L
  }
}
