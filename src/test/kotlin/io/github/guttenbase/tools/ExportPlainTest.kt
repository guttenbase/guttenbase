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
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8

/**
 * Create a plain SQL dump of the source database
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportPlainTest : AbstractGuttenBaseTest() {
  private val exportPlainConnectorInfo = ExportPlainTextConnectorInfo(SOURCE, FILE, "", DatabaseType.H2DB, UTF_8)

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

    val dataScript = File(FILE).readLines(UTF_8)
    assertThat(dataScript).contains("	(3, 'Häagen daß', 'Y');")

    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, ddlScript)
    ScriptExecutorTool(connectorRepository).executeScript(TARGET, false, true, dataScript)

    CheckEqualTableDataTool(connectorRepository).checkTableData(SOURCE, TARGET)
  }

  @Test
  fun `Explicit encoding`() {
    val exportPlainConnectorInfo = ExportPlainTextConnectorInfo(SOURCE, FILE, "", DatabaseType.H2DB, ISO_8859_1)
    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, SCRIPT)

    val dataScriptUtf8 = File(FILE).readLines(UTF_8)
    val dataScriptIso = File(FILE).readLines(ISO_8859_1)

    assertThat(dataScriptUtf8).contains("	(3, 'H�agen da�', 'Y');")
    assertThat(dataScriptIso).contains("	(3, 'Häagen daß', 'Y');")
  }

  companion object {
    private const val FILE = "./target/dump.sql"

    const val SOURCE = "SOURCE"
    const val SCRIPT = "SCRIPT"
    const val TARGET = "TARGET"
  }
}