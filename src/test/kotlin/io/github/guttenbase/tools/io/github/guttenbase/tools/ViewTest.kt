@file:Suppress("JavaIoSerializableObjectMustHaveReadResolve")

package io.github.guttenbase.io.github.guttenbase.tools.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.hints.SOURCE
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Reda view data
 *
 * &copy; 2025-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ViewTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestH2ConnectionInfo())
      .addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())

    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/data/test-data.sql")
  }

  @Test
  fun `Read from view`() {
    assertThat(connectorRepository.getDatabase(SOURCE).views).hasSize(1).extracting<String> { it.tableName }.containsExactly("VIEW_DATA")
//    val data = ReadTableDataTool(connectorRepository, SOURCE, "VIEW_DATA", true).start().use { it.readTableData(-1) }
//
//    assertThat(data).hasSize(1)
  }
}
