package io.github.guttenbase.hints

import io.github.guttenbase.io.github.guttenbase.tools.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.repository.RepositoryColumnFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters columns when inquiring connector repository.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class RepositoryColumnFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
  }

  @Test
  fun testFilter() {
    val tableMetaData1 = connectorRepository.getDatabase(SOURCE).getTable("FOO_USER")!!

    assertEquals(7, tableMetaData1.columnCount, "Before")
    connectorRepository.addConnectorHint(SOURCE, object : RepositoryColumnFilterHint() {
      override val value: RepositoryColumnFilter
        get() = RepositoryColumnFilter { column -> !column.columnName.equals("password", ignoreCase = true) }
    })

    val tableMetaData2 = connectorRepository.getDatabase(SOURCE).getTable("FOO_USER")!!
    assertEquals(6, tableMetaData2.columnCount, "After")
  }
}
