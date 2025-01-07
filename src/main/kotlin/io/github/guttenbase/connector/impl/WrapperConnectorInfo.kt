package io.github.guttenbase.connector.impl

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection

typealias CLOSER = (Connection) -> Unit

/**
 * Connection info via alternate connection provider
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
@Suppress("unused")
open class WrapperConnectorInfo @JvmOverloads constructor(
  override val schema: String,
  override val databaseType: DatabaseType,
  private val connectionProvider: () -> Connection,
  private val connectionCloser: CLOSER? = null
) : ConnectorInfo {

  override val user: String = ""
  override val password: String = ""

  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): Connector =
    object : AbstractConnector(connectorRepository, connectorId, this@WrapperConnectorInfo) {
      override fun openConnection(): Connection {
        connection = connectionProvider.invoke()
        return connection
      }

      override fun closeConnection() {
        if (connectionCloser != null) {
          connectionCloser.invoke(connection)
        } else {
          super.closeConnection()
        }
      }
    }
}