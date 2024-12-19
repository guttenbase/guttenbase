package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import org.slf4j.LoggerFactory
import java.sql.Connection

/**
 * Abstract base implementation of data base configuration.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("SqlSourceToSinkFlow")
abstract class AbstractDatabaseConfiguration(protected val connectorRepository: ConnectorRepository) :
  DatabaseConfiguration {
  /**
   * Execute single statement.
   */
  protected fun executeSQL(connection: Connection, vararg sqls: String) {
    connection.createStatement().use {
      for (sql in sqls) {
        LOG.info("Executing: $sql")

        it.execute(sql)
      }
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
