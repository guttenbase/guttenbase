package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.repository.RepositoryTableFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters tables when inquiring connector repository.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class RepositoryTableFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
  }

  @Test
  fun testFilter() {
    val tableMetaData1 = connectorRepository.getDatabase(SOURCE).tables
    assertEquals( 6, tableMetaData1.size, "Before")

    connectorRepository.addConnectorHint(SOURCE, object : RepositoryTableFilterHint() {
      override val value: RepositoryTableFilter
        get() = RepositoryTableFilter { it.tableName.uppercase().contains("USER") }
    })

    val tableMetaData2 = connectorRepository.getDatabase(SOURCE).tables
    assertEquals( 3, tableMetaData2.size,"After")
  }
}
