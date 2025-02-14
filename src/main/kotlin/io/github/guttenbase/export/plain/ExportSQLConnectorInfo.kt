package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

typealias TEMPLATE_SUPPLIER = (DatabaseType) -> InputStream?

val DEFAULT_TEMPLATE_SUPPLIER: TEMPLATE_SUPPLIER =
  { ExportSQLConnectorInfo::class.java.getResourceAsStream("/dbmetadata/${it.name}.json") }

/**
 * Connection info for exporting data to a (optionally compressed) file.
 *
 * &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
data class ExportSQLConnectorInfo
@JvmOverloads
constructor(
  internal val sourceConnectorId: String,
  override val databaseType: DatabaseType,
  internal val outputStream: OutputStream,
  internal val databaseTemplateSupplier: TEMPLATE_SUPPLIER = DEFAULT_TEMPLATE_SUPPLIER,
  override val schema: String = "",
  internal val encoding: Charset = Charsets.UTF_8,
  internal val compress: Boolean = false
) : ConnectorInfo {
  @JvmOverloads
  constructor(
    sourceConnectorId: String,
    databaseType: DatabaseType, path: String,
    databaseTemplateSupplier: TEMPLATE_SUPPLIER = DEFAULT_TEMPLATE_SUPPLIER,
    schema: String = "", encoding: Charset = Charsets.UTF_8, compress: Boolean = false
  ) : this(sourceConnectorId, databaseType, FileOutputStream(path), databaseTemplateSupplier, schema, encoding, compress)

  override val user: String get() = "user"
  override val password: String get() = "password"

  private lateinit var exportPlainConnector: ExportSQLConnector

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): ExportSQLConnector {
    if (!this::exportPlainConnector.isInitialized) {
      exportPlainConnector = ExportSQLConnector(connectorRepository, this)
    }

    return exportPlainConnector
  }
}

