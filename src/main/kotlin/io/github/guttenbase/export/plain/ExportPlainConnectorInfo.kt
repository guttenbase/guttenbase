package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Connection info for exporting data to a file.
 *
 *  &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
data class ExportPlainConnectorInfo
@JvmOverloads constructor(
  internal val sourceConnectorId: String,
  private val outputStream: OutputStream,
  override val databaseType: DatabaseType = DatabaseType.GENERIC
) : ConnectorInfo {
  @JvmOverloads
  constructor(sourceConnectorId: String, path: String, databaseType: DatabaseType = DatabaseType.GENERIC)
      : this(sourceConnectorId, FileOutputStream(path), databaseType)

  override val user: String get() = "user"
  override val password: String get() = "password"
  override val schema: String get() = "schema"

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String) =
    ExportPlainConnector(connectorRepository, connectorId, this)

  companion object {
    private const val serialVersionUID = 1L
  }
}
