package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for MS Server SQL data base.
 *
 *
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
open class MsSqlSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
