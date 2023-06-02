package io.github.guttenbase.connector.impl

import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException


/**
 * Connection info via data source and optional user/password.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DataSourceConnector(
  connectorRepository: ConnectorRepository, connectorId: String,
  dataSourceConnectionInfo: DataSourceConnectorInfo
) : AbstractConnector(connectorRepository, connectorId, dataSourceConnectionInfo) {
  private val dataSourceConnectionInfo: DataSourceConnectorInfo get() = connectionInfo as DataSourceConnectorInfo

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun openConnection(): Connection {
    if (!connectionReady()) {
      connection =
        dataSourceConnectionInfo.dataSource.getConnection(
          dataSourceConnectionInfo.user, dataSourceConnectionInfo.password
        )
    }

    return connection
  }
}