package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.defaults.impl.DefaultDatabaseTableFilter
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.DatabaseTableFilter
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters columns when inquiring the data base.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DatabaseTableFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables.sql")
    connectorRepository.addConnectorHint(SOURCE, object : DatabaseTableFilterHint() {
      override val value: DatabaseTableFilter
        get() = object : DefaultDatabaseTableFilter() {
          override fun accept(table: TableMetaData): Boolean {
            return table.tableName.uppercase().contains("USER")
          }
        }
    })
  }

  @Test
  fun testFilter() {
    val tableMetaData = connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData
    assertEquals(3, tableMetaData.size)
    for (table in tableMetaData) {
      assertTrue(table.tableName.uppercase().contains("USER"))
      assertEquals("TABLE", table.tableType)
    }
  }

  companion object {
    const val SOURCE = "SOURCE"
  }
}
