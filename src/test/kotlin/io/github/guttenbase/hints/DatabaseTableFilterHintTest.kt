package io.github.guttenbase.hints

import io.github.guttenbase.io.github.guttenbase.tools.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.defaults.impl.DefaultDatabaseTableFilter
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.repository.DatabaseTableFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters columns when inquiring the data base.
 *
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
class DatabaseTableFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
    connectorRepository.addConnectorHint(SOURCE, object : DatabaseTableFilterHint() {
      override val value: DatabaseTableFilter
        get() = object : DefaultDatabaseTableFilter() {
          override fun accept(table: DatabaseEntityMetaData): Boolean {
            return table.tableName.uppercase().contains("USER")
          }
        }
    })
  }

  @Test
  fun testFilter() {
    val tableMetaData = connectorRepository.getDatabase(SOURCE).tables
    assertEquals(3, tableMetaData.size)
    for (table in tableMetaData) {
      assertTrue(table.tableName.uppercase().contains("USER"))
      assertEquals("TABLE", table.tableType)
    }
  }
}
