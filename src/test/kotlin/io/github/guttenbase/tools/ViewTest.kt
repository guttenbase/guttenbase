package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.H2DB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.tools.ReadTableDataTool
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Read view data
 *
 * &copy; 2025-2044 tech@spree
 *
 * @author M. Dahm
 */
class ViewTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(H2DB, TestH2ConnectionInfo())
      .addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())

    scriptExecutorTool.executeFileScript(H2DB, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(DERBYDB, resourceName = "/ddl/tables-derby.sql")
    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/ddl/tables-hsqldb.sql")
    scriptExecutorTool.executeFileScript(H2DB, resourceName = "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(DERBYDB, resourceName = "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/data/test-data.sql")
  }

  @Test
  fun `Read from view`() {
    test(H2DB)
    test(DERBYDB)
    test(H2DB)
  }

  private fun test(connectorId: String) {
    val views = connectorRepository.getDatabase(connectorId).views
    Assertions.assertThat(views).hasSize(1).extracting<String> { it.tableName }.containsExactly("VIEW_DATA")
    Assertions.assertThat(views[0].viewDefinition.replace('\n', ' ')).contains("SELECT DISTINCT")

    val data = ReadTableDataTool(connectorRepository, connectorId, "VIEW_DATA", true).start().use { it.readTableData(-1) }
    Assertions.assertThat(data.map { it.values }.flatten()).containsExactly("Role 1", "Role 3")
  }
}