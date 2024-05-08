package io.github.guttenbase.export.plain

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import java.sql.Connection

/**
 * Connection info for exporting data to a file.
 *
 *  &copy; 2024-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportPlainConnector(
  private val connectorRepository: ConnectorRepository,
  internal val connectorInfo: ExportPlainTextConnectorInfo
) : Connector {
  internal lateinit var connection: ExportPlainTextConnection

  override fun openConnection(): Connection {
    connection = ExportPlainTextConnection(this)
    return connection
  }

  override fun closeConnection() {
    connection.close()
  }

  /**
   * Table metadata is the same as the metadata of the source connector. The only difference is that the row count
   * of all tables is reset to 0 and teh database type is set
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

    return object : InternalDatabaseMetaData by data {
      override val databaseType: DatabaseType
        get() = connectorInfo.databaseType

      override val tableMetaData: List<TableMetaData>
        get() = tableMetaData
    }
  }

  private fun retrieveSourceDatabaseMetaData(): InternalDatabaseMetaData =
    connectorRepository.getDatabaseMetaData(connectorInfo.sourceConnectorId) as InternalDatabaseMetaData
}
