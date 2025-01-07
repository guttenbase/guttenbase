package io.github.guttenbase.connector.impl

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository

/**
 * Connection info via explicit URL and driver.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class URLConnectorInfoImpl(
  override val url: String, override val user: String, override val password: String, override val driver: String,
  override val schema: String, override val databaseType: DatabaseType
) : URLConnectorInfo {
  /**
   * {@inheritDoc}
   */
  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): Connector =
    URLConnector(connectorRepository, connectorId, this)

  override fun toString(): String {
    return "URLConnectorInfoImpl{" +
        "url='" + url + '\'' +
        ", user='" + user + '\'' +
        ", password='********" + '\'' +
        ", driver='" + driver + '\'' +
        ", schema='" + schema + '\'' +
        ", databaseType=" + databaseType +
        '}'
  }

  companion object {
    @Suppress("unused")
    private const val serialVersionUID = 1L
  }
}