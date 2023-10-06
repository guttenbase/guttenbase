package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ReadTableDataTool
import io.github.guttenbase.tools.InsertStatementTool
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
    connectorRepository.addConnectionInfo(SOURCE, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(H2, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(DERBY, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-h2.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/data/test-data.sql")
  }

  @Test
  fun testDerby() {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, DERBY)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, DERBY)

    InsertStatementTool(connectorRepository, DERBY).createInsertStatement(
      "FOO_COMPANY",
      includePrimaryKey = true
    ).setParameter("SUPPLIER", 'x').setParameter("NAME", "JENS")
      .setParameter("ID", 4711L)
      .execute()

    ReadTableDataTool(connectorRepository, DERBY, "FOO_USER").start().use {
      val data = it.readTableData(10)
      println(data)
    }
  }

  @Test
  fun testHSQLDB() {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, HSQLDB)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, HSQLDB)
  }

  @Test
  fun testH2() {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, H2)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, H2)
  }

  companion object {
    const val SOURCE = "SOURCE"
    const val DERBY = "TARGET"
    const val H2 = "H2"
    const val HSQLDB = "HSQLDB"
  }
}
