package io.github.guttenbase.connector.impl

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import javax.sql.DataSource

/**
 * Connector info via data source with optional user/password. To be used when running in an application server.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DataSourceConnectorInfo(
  val dataSource: DataSource,
  override val user: String,
  override val password: String,
  override val schema: String,
  override val databaseType: DatabaseType
) : ConnectorInfo {

  /**
   * {@inheritDoc}
   */
  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String) =
    DataSourceConnector(connectorRepository, connectorId, this)

  companion object {
    private const val serialVersionUID = 1L
  }
}