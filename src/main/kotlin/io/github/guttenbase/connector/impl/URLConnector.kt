package io.github.guttenbase.connector.impl

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Connection info via explicit URL and driver.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class URLConnector(connectorRepository: ConnectorRepository, connectorId: String, urlConnectionInfo: URLConnectorInfo) :
  AbstractURLConnector(connectorRepository, connectorId, urlConnectionInfo) {
}