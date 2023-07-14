package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for generic/yet unhandled data base.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class GenericTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository)
