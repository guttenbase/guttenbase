package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for IBM DB2 data base.
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class Db2SourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
