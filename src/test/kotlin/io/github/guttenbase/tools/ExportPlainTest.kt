package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.export.plain.ExportPlainTextConnectorInfo
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.CheckEqualTableDataTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Create a plain SQL dump of the source database
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportPlainTest : AbstractGuttenBaseTest() {
  private val exportPlainConnectorInfo = ExportPlainTextConnectorInfo(SOURCE, FILE, DatabaseType.H2DB)

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Export SQL data`() {
    val ddlScript = CopySchemaTool(connectorRepository).createDDLScript(SOURCE, SCRIPT)
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, SCRIPT)
    val dataScript = File(FILE).readLines()

    assertTrue(dataScript.isNotEmpty())

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