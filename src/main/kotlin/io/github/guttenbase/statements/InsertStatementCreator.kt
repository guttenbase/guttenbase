package io.github.guttenbase.statements

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Create INSERT statement with multiple VALUES-tuples.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class InsertStatementCreator(connectorRepository: ConnectorRepository, targetConnectorId: String) :
  AbstractInsertStatementCreator(connectorRepository, targetConnectorId)
