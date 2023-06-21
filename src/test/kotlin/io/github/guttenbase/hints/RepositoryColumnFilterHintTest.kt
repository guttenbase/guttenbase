package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.repository.RepositoryColumnFilter
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters columns when inquiring connector repository.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class RepositoryColumnFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach

  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables.sql")
  }

  @Test
  fun testFilter() {
    assertEquals(6, connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")?.columnCount, "Before")
    connectorRepository.addConnectorHint(SOURCE, object : RepositoryColumnFilterHint() {
      override val value: RepositoryColumnFilter
        get() = RepositoryColumnFilter { column -> !column.columnName.equals("password", ignoreCase = true) }
    })

    assertEquals(5, connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")?.columnCount, "After")
  }
}
