package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for HSQLDB data base.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class HsqldbSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
