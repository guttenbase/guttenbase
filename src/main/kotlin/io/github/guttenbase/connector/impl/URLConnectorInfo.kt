package io.github.guttenbase.connector.impl

import io.github.guttenbase.connector.ConnectorInfo

/**
 * Connector info via explicit URL and driver.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface URLConnectorInfo : ConnectorInfo {
  val url: String
  val driver: String
}