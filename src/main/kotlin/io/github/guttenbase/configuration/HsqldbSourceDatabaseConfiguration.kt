package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for HSQLDB data base.
 *
 *
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
open class HsqldbSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
