package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for Derby data base. Derby does not support tompariliy disabling (FK) constraints
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DerbyTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository)
