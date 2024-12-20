package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.charset.Charset

/**
 * Connection info for exporting data to a (optionally compressed) file.
 *
 *  &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
data class ExportPlainTextConnectorInfo
@JvmOverloads constructor(
  internal val sourceConnectorId: String,
  internal val sourceDatabaseType: DatabaseType,
  internal val outputStream: OutputStream,
  override val schema: String = "",
  internal val encoding: Charset = Charsets.UTF_8,
  internal val compress: Boolean = false
) : ConnectorInfo {
  @JvmOverloads
  constructor(
    sourceDatabase: DatabaseMetaData,
    path: String,
    schema: String = "",
    encoding: Charset = Charsets.UTF_8,
    compress: Boolean = false
  ) : this(sourceDatabase.connectorId, sourceDatabase.databaseType, FileOutputStream(path), schema, encoding, compress)

  override val user: String get() = "user"
  override val password: String get() = "password"
  override val databaseType = sourceDatabaseType

  private lateinit var exportPlainConnector: ExportPlainConnector

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): ExportPlainConnector {
    if (!this::exportPlainConnector.isInitialized) {
      exportPlainConnector = ExportPlainConnector(connectorRepository, this)
    }

    return exportPlainConnector
  }

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}
