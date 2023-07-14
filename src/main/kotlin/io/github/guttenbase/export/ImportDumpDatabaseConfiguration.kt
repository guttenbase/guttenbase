package io.github.guttenbase.export

import io.github.guttenbase.configuration.DefaultSourceDatabaseConfiguration
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

/**
 * Import dump configuration forwards important events to [ImportDumpConnection].
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ImportDumpDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository) {
  override fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ImportDumpConnection).initializeReadTable(table)
  }

  override fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    (connection as ImportDumpConnection).finalizeReadTable(table)
  }
}
