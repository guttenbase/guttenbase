package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.export.plain.ExportSQLConnectorInfo
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.hints.TARGET
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.CheckEqualTableDataTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import kotlin.text.Charsets.ISO_8859_1
import kotlin.text.Charsets.UTF_8

/**
 * Create a plain SQL dump of the source database
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportSQLTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestHsqlConnectionInfo())

    val sourceDatabase = connectorRepository.getDatabaseMetaData(SOURCE)
    val exportPlainConnectorInfo = ExportSQLConnectorInfo(sourceDatabase, FILE, "", UTF_8)

    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Export SQL data`() {
    DefaultTableCopyTool(connectorRepository, SOURCE, SCRIPT).copyTables()
    val dataScript = File(FILE).readLines(UTF_8)
    assertThat(dataScript).contains("	(3, 'Häagen daß', 'Y');")

    val ddlScript = CopySchemaTool(connectorRepository, SOURCE, SCRIPT).createDDLScript()

    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, ddlScript)
    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, dataScript)

    CheckEqualTableDataTool(connectorRepository, SOURCE, TARGET).checkTableData()
  }

  @Test
  fun `Compress data`() {
    val sourceDatabase = connectorRepository.getDatabaseMetaData(SOURCE)
    val compressedInfo = ExportSQLConnectorInfo(sourceDatabase, FILE, "", UTF_8, true)
    connectorRepository.addConnectionInfo(COMPRESSED, compressedInfo)

    DefaultTableCopyTool(connectorRepository, SOURCE, COMPRESSED).copyTables()
    val contentType = Files.probeContentType(File(FILE).toPath())

    assertThat(contentType).isEqualTo("application/x-gzip-compressed")
  }

  @Test
  fun `Explicit encoding`() {
    val sourceDatabase = connectorRepository.getDatabaseMetaData(SOURCE)
    val exportPlainConnectorInfo = ExportSQLConnectorInfo(sourceDatabase, FILE, "", ISO_8859_1)
    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    DefaultTableCopyTool(connectorRepository, SOURCE, SCRIPT).copyTables()

    val dataScriptUtf8 = File(FILE).readLines(UTF_8)
    val dataScriptIso = File(FILE).readLines(ISO_8859_1)

    assertThat(dataScriptUtf8).contains("	(3, 'H�agen da�', 'Y');")
    assertThat(dataScriptIso).contains("	(3, 'Häagen daß', 'Y');")
  }

  companion object {
    private const val FILE = "./target/dump.sql"

    const val SCRIPT = "SCRIPT"
    const val COMPRESSED = "COMPRESSED"
  }
}