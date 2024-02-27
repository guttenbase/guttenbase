package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.InsertStatementTool
import io.github.guttenbase.tools.ReadTableDataTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.assertj.core.api.Assertions
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
      .addConnectionInfo(H2, TestH2ConnectionInfo()).addConnectionInfo(DERBY, TestDerbyConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-h2.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/data/test-data.sql")
  }

  @Test
  fun testDerby() {
    test(DERBY)
  }

  @Test
  fun testHSQLDB() {
    test(HSQLDB)
  }

  @Test
  fun testH2() {
    test(H2)
  }

  private fun test(target: String) {
    CopySchemaTool(connectorRepository).copySchema(SOURCE, target)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, target)

    val databaseMetaData = connectorRepository.getDatabaseMetaData(target)
    val tableMetaData = databaseMetaData.getTableMetaData("FOO_COMPANY")!!

    Assertions.assertThat(tableMetaData.totalRowCount).isEqualTo((4))
    Assertions.assertThat(tableMetaData.filteredRowCount).isEqualTo((4))
    Assertions.assertThat(tableMetaData.minId).isEqualTo((1))
    Assertions.assertThat(tableMetaData.maxId).isEqualTo((4))

    // Explicit ID
    InsertStatementTool(connectorRepository, target).createInsertStatement(
      "FOO_COMPANY",
      includePrimaryKey = true
    ).setParameter("SUPPLIER", 'x').setParameter("NAME", "JENS")
      .setParameter("ID", 0L)
      .execute()

    // Implicit ID
    InsertStatementTool(connectorRepository, target).createInsertStatement(
      "FOO_COMPANY",
      includePrimaryKey = false
    ).setParameter("SUPPLIER", 'x').setParameter("NAME", "HIPPE")
      .execute()

    ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use { tool ->
      val data = tool.readTableData(-1).sortedBy { it["ID"].toString().toInt() }

      Assertions.assertThat(data).hasSize(6)
      val first = data.first()
      Assertions.assertThat(first).hasSize(3)
      Assertions.assertThat(first["NAME"]).isEqualTo("JENS")
      Assertions.assertThat(first["ID"]).isEqualTo(0L)

      val last = data.last()
      Assertions.assertThat(last).hasSize(3)
      Assertions.assertThat(last["NAME"]).isEqualTo("HIPPE")
      Assertions.assertThat(last["ID"]).isEqualTo(5L)
    }
  }

  companion object {
    const val SOURCE = "SOURCE"
    const val DERBY = "TARGET"
    const val H2 = "H2"
    const val HSQLDB = "HSQLDB"
  }
}
