package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for MYSQL data base.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class MySqlSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
