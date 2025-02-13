package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import io.github.guttenbase.repository.ConnectorRepository
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
   * Table metadata is the same as the metadata of the source connector. The only difference is that the row count
   * of all tables is reset to 0 and the database type is set
   *
   * {@inheritDoc}
   */
  override fun retrieveDatabaseMetaData(): DatabaseMetaData {
    val data = retrieveSourceDatabaseMetaData()
    val tableMetaData = Util.copyObject(InternalDatabaseMetaData::class.java, data).tableMetaData

    tableMetaData.map { it as InternalTableMetaData }.forEach {
      it.totalRowCount = 0
      it.filteredRowCount = 0
    }

    val tableMetaDataMap = tableMetaData.associateBy { it.tableName.uppercase() }

    return object : InternalDatabaseMetaData by data {
      override val databaseType get() = connectorInfo.databaseType

      override val tableMetaData get() = tableMetaData

      override fun getTableMetaData(tableName: String) = tableMetaDataMap[tableName.uppercase()]

      override val schema get() = connectorInfo.schema.ifBlank { data.schema }

      override val schemaPrefix get() = if (schema.isNotBlank()) "$schema." else ""
    }
  }

  private fun retrieveSourceDatabaseMetaData() =
    connectorRepository.getDatabaseMetaData(connectorInfo.sourceConnectorId) as InternalDatabaseMetaData
}
