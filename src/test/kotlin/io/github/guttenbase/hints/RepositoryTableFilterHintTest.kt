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
    val tableMetaData1 = connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData
    assertEquals( 6, tableMetaData1.size, "Before")

    connectorRepository.addConnectorHint(SOURCE, object : RepositoryTableFilterHint() {
      override val value: RepositoryTableFilter
        get() = RepositoryTableFilter { it.tableName.uppercase().contains("USER") }
    })

    val tableMetaData2 = connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData
    assertEquals( 3, tableMetaData2.size,"After")
  }
}
