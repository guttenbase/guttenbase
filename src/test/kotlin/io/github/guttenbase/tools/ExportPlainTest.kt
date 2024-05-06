package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.export.plain.ExportPlainConnectorInfo
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.CheckEqualTableDataTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Create a plain SQL dump of the source database
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportPlainTest : AbstractGuttenBaseTest() {
  private val exportPlainConnectorInfo = ExportPlainConnectorInfo(SOURCE, FILE, DatabaseType.H2DB)

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-derby.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Export SQL data`() {
    val ddlScript = CopySchemaTool(connectorRepository).createDDLScript(SOURCE, SCRIPT)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, SCRIPT)
    val dataScript = exportPlainConnectorInfo.statements

    assertTrue(dataScript.isNotEmpty())
    assertTrue(dataScript.contains("INSERT INTO FOO_ROLE (FIXED_ROLE, ID, ROLE_NAME) VALUES ('Y', 1, 'Role 1'), ('Y', 2, 'Role 2'), ('Y', 3, 'Role 3'), ('Y', 4, 'Role 4');"))

    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, ddlScript)
    ScriptExecutorTool(connectorRepository).executeScript(TARGET, false, true, dataScript)

    CheckEqualTableDataTool(connectorRepository).checkTableData(SOURCE, TARGET)
  }

  companion object {
    private const val FILE = "./target/dump.sql"
    const val SOURCE = "SOURCE"
    const val SCRIPT = "SCRIPT"
    const val TARGET = "TARGET"
  }
}