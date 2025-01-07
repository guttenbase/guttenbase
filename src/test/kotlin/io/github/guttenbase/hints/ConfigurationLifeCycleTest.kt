package io.github.guttenbase.hints

import io.github.guttenbase.configuration.DefaultSourceDatabaseConfiguration
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.TableMetaData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import java.sql.Connection

/**
 * Check that "life-cycle" methods are called correctly
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ConfigurationLifeCycleTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-hsqldb.sql", "/data/test-data.sql") {
  private var afterTableCopy = 0
  private var beforeTableCopy = 0
  private var afterSelect = 0
  private var beforeSelect = 0
  private var finalizeSourceConnection = 0
  private var initializeSourceConnection = 0

  @BeforeEach
  fun setup() {
    connectorRepository.addSourceDatabaseConfiguration(
      DatabaseType.DERBY,
      object : DefaultSourceDatabaseConfiguration(connectorRepository) {
        override fun initializeSourceConnection(connection: Connection, connectorId: String) {
          super.initializeSourceConnection(connection, connectorId)
          initializeSourceConnection++
        }

        override fun finalizeSourceConnection(connection: Connection, connectorId: String) {
          finalizeSourceConnection++
        }

        override fun beforeSelect(connection: Connection, connectorId: String, table: TableMetaData) {
          beforeSelect++
        }

        override fun afterSelect(connection: Connection, connectorId: String, table: TableMetaData) {
          afterSelect++
        }

        override fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
          beforeTableCopy++
        }

        override fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
          afterTableCopy++
        }
      })
  }

  override fun executeChecks() {
    val toolsCount = 3
    val tablesCount = 6

    assertEquals(toolsCount, initializeSourceConnection)
    assertEquals(toolsCount, finalizeSourceConnection)
    assertEquals(tablesCount, beforeTableCopy)
    assertEquals(tablesCount, afterTableCopy)
    assertEquals(2 * tablesCount, beforeSelect)
    assertEquals(2 * tablesCount, afterSelect)
  }
}
