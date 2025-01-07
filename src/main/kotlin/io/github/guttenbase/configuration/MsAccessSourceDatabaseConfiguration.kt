package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository


/**
 * Implementation for MS Access via UCanAccess.
 *
 *
 * http://ucanaccess.sourceforge.net/site.html
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class MsAccessSourceDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultSourceDatabaseConfiguration(connectorRepository)
