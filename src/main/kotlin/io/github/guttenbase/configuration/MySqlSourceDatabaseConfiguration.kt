package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for MYSQL data base.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class MySqlSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
