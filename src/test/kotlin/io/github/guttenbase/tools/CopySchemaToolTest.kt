package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Copy schema between databases
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class CopySchemaToolTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(DERBY, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables.sql")
  }

  @Test
  fun testDerby() {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, DERBY)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, DERBY)
  }

  @Test
  fun testHSQLDB() {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, HSQLDB)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, HSQLDB)
  }

  companion object {
    const val SOURCE = "SOURCE"
    const val DERBY = "TARGET"
    const val HSQLDB = "HSQLDB"
  }
}
