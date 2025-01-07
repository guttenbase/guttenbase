package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for PostgreSQL data base.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class PostgresqlSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
