package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException

/**
 * Abstract base implementation of data base configuration.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
abstract class AbstractDatabaseConfiguration(protected val connectorRepository: ConnectorRepository) : DatabaseConfiguration {
  /**
   * Execute single statement.
   */
  @Throws(SQLException::class)
  protected fun executeSQL(connection: Connection, sql: String) {
    LOG.debug("Executing: $sql")

    connection.createStatement().use {
      it.execute(sql)
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {}

  /**
   * {@inheritDoc}
   */
  override fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {}

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(DatabaseConfiguration::class.java)
  }
}
