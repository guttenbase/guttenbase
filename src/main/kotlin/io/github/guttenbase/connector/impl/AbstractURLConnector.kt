package io.github.guttenbase.connector.impl

import io.github.guttenbase.repository.ConnectorRepository
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.DriverManager
import java.sql.DriverManager.getConnection
import java.sql.SQLException


/**
 * Connection info via explicit URL and driver.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
abstract class AbstractURLConnector(
  connectorRepository: ConnectorRepository, connectorId: String,
  urlConnectionInfo: URLConnectorInfo
) : AbstractConnector(connectorRepository, connectorId, urlConnectionInfo) {
  private val urlConnectionInfo: URLConnectorInfo get() = connectionInfo as URLConnectorInfo

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun openConnection(): Connection {
    if (!connectionReady()) {
      try {
        Class.forName(urlConnectionInfo.driver).getDeclaredConstructor().newInstance()
      } catch (e: Exception) {
        LOG.error("JDBC driver not found", e)
        throw SQLException("Creating JDBC driver", e)
      }

      connection = getConnection(urlConnectionInfo.url, urlConnectionInfo.user, urlConnectionInfo.password)
    }

    return connection
  }

  companion object {
    @JvmStatic
    protected val LOG = LoggerFactory.getLogger(AbstractURLConnector::class.java)!!
  }
}