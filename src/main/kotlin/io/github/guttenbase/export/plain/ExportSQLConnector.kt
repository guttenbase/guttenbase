package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.DatabaseMetaDataExporterTool.Companion.importDataBaseMetaData
import io.github.guttenbase.utils.Util
import java.sql.Connection

/**
 * Connector for exporting DDL and data to a file.
 *
 * &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportSQLConnector(
  private val connectorRepository: ConnectorRepository,
  internal val connectorId: String,
  internal val connectorInfo: ExportSQLConnectorInfo
) : Connector {
  internal lateinit var connection: ExportSQLConnection

  override fun openConnection(): Connection {
    connection = ExportSQLConnection(this)
    return connection
  }

  override fun closeConnection() {
    connection.close()
  }

  /**
   * Database meta data is created as an empty instance of the intended target database. The required (offline) information
   * is read from the supplied file (JSON).
   *
   * {@inheritDoc}
   */
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    val supplier = connectorInfo.databaseTemplateSupplier(connectorInfo.databaseType)
      ?: throw GuttenBaseException("Database template supplier resolves to null for ${connectorInfo.databaseType}")

    val targetDatabase = importDataBaseMetaData(supplier, connectorId, connectorRepository)
    val sourceDatabase = retrieveSourceDatabaseMetaData()
    val tableMetaData = Util.copyObject(InternalDatabaseMetaData::class.java, sourceDatabase).tableMetaData

    tableMetaData.map { it as InternalTableMetaData }.forEach {
      it.totalRowCount = 0
      it.filteredRowCount = 0
      it.database = targetDatabase
    }
    val tableMetaDataMap = tableMetaData.associateBy { it.tableName.uppercase() }

    return object : InternalDatabaseMetaData by targetDatabase {
      override val databaseType get() = connectorInfo.databaseType

      override val schema get() = connectorInfo.schema.ifBlank { targetDatabase.schema }

      override val schemaPrefix get() = if (schema.isNotBlank()) "$schema." else ""

      override val tableMetaData get() = tableMetaData

      override fun getTable(tableName: String) = tableMetaDataMap[tableName.uppercase()]
    }
  }

  private fun retrieveSourceDatabaseMetaData() =
    connectorRepository.getDatabaseMetaData(connectorInfo.sourceConnectorId) as InternalDatabaseMetaData
}
