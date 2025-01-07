package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for H2 DB data base.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class H2DbSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
