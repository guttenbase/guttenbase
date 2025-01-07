package io.github.guttenbase.export

import io.github.guttenbase.configuration.DefaultTargetDatabaseConfiguration
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

/**
 * Export dump configuration forwards important events to [ExportDumpConnection].
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  override fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData) {
    System.gc()
  }

  override fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ExportDumpConnection).initializeWriteTableData(table)
  }

  override fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ExportDumpConnection).finalizeWriteTableData(table)
  }

  override fun beforeNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ExportDumpConnection).initializeWriteRowData(table)
  }

  override fun afterNewRow(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ExportDumpConnection).finalizeWriteRowData(table)
  }
}
