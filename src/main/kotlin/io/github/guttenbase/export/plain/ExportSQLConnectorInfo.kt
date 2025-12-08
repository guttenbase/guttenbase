package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util.DEFAULT_ENCODING
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset

typealias TEMPLATE_SUPPLIER = (DatabaseType) -> InputStream?

@JvmField
val DEFAULT_TEMPLATE_SUPPLIER: TEMPLATE_SUPPLIER =
  { ExportSQLConnectorInfo::class.java.getResourceAsStream("/dbmetadata/${it.name}.json") }

/**
 * Connection info for exporting data to a (optionally compressed) file.
 *
 * &copy; 2024-2044 tech@spree
 *
 * @author M. Dahm
 */
data class ExportSQLConnectorInfo
@JvmOverloads
constructor(
  internal val sourceConnectorId: String,
  override val databaseType: DatabaseType,
  internal val outputStream: OutputStream,
  override val schema: String = "",
  internal val encoding: Charset = DEFAULT_ENCODING,
  internal val compress: Boolean = false,
  internal val databaseTemplateSupplier: TEMPLATE_SUPPLIER = DEFAULT_TEMPLATE_SUPPLIER
) : ConnectorInfo {
  @JvmOverloads
  constructor(
    sourceConnectorId: String, databaseType: DatabaseType, path: String,
    schema: String = "", encoding: Charset = DEFAULT_ENCODING, compress: Boolean = false,
    databaseTemplateSupplier: TEMPLATE_SUPPLIER = DEFAULT_TEMPLATE_SUPPLIER
  ) : this(sourceConnectorId, databaseType, FileOutputStream(path), schema, encoding, compress, databaseTemplateSupplier)

  override val user: String get() = "user"
  override val password: String get() = "password"

  private lateinit var exportPlainConnector: ExportSQLConnector

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): ExportSQLConnector {
    if (!this::exportPlainConnector.isInitialized) {
      exportPlainConnector = ExportSQLConnector(connectorRepository, connectorId, this)
    }

    return exportPlainConnector
  }
}

