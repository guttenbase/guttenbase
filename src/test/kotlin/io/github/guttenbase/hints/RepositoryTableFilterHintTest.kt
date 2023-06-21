package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.repository.RepositoryTableFilter
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters tables when inquiring connector repository.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class RepositoryTableFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables.sql")
  }

  @Test
  fun testFilter() {
    assertEquals( 6, connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData.size, "Before")
    connectorRepository.addConnectorHint(SOURCE, object : RepositoryTableFilterHint() {
      override val value: RepositoryTableFilter
        get() = RepositoryTableFilter { it.tableName.uppercase().contains("USER") }
    })

    assertEquals( 3, connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData.size,"After")
  }
}
