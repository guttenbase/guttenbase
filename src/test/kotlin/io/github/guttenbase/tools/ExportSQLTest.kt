package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.export.plain.ExportSQLConnectorInfo
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.hints.TARGET
import io.github.guttenbase.meta.DatabaseType
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
    connectorRepository.addConnectionInfo(SCRIPT, ExportSQLConnectorInfo(SOURCE, DatabaseType.HSQLDB, FILE_HSQL, "", UTF_8))
    connectorRepository.addConnectionInfo(MYSQL, ExportSQLConnectorInfo(SOURCE, DatabaseType.MYSQL, FILE_MYSQL, "lokal", UTF_8))

    ScriptExecutorTool(connectorRepository, encoding = UTF_8).executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
    ScriptExecutorTool(connectorRepository, encoding = UTF_8).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Export SQL data`() {
    DefaultTableCopyTool(connectorRepository, SOURCE, SCRIPT).copyTables()
    val dataScript = File(FILE_HSQL).readLines(UTF_8)
    assertThat(dataScript).contains(TEST_STRING)
      .contains("""INSERT INTO "FOO_ROLE" ("ID", "FIXED_ROLE", "ROLE_NAME") VALUES""")

    val ddlScript = CopySchemaTool(connectorRepository, SOURCE, SCRIPT).createDDLScript()
    assertThat(ddlScript.joinToString("\n")).contains("""CREATE TABLE IF NOT EXISTS "FOO_ROLE"""")

    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, ddlScript)
    ScriptExecutorTool(connectorRepository).executeScript(TARGET, true, true, dataScript)

    CheckEqualTableDataTool(connectorRepository, SOURCE, TARGET).checkTableData()
  }

  @Test
  fun `Export to MySQL`() {
    DefaultTableCopyTool(connectorRepository, SOURCE, MYSQL).copyTables()

    val dataScript = File(FILE_MYSQL).readLines(UTF_8)
    assertThat(dataScript).contains(TEST_STRING)
      .contains("""INSERT INTO lokal.`FOO_ROLE` (`ID`, `FIXED_ROLE`, `ROLE_NAME`) VALUES""")

    val ddlScript = CopySchemaTool(connectorRepository, SOURCE, MYSQL).createDDLScript()
    assertThat(ddlScript.joinToString("\n")).contains("lokal.`FOO_USER`")
  }

  @Test
  fun `Compress data`() {
    val compressedInfo =
      ExportSQLConnectorInfo(SOURCE, DatabaseType.HSQLDB, path = FILE_HSQL, schema = "", encoding = UTF_8, compress = true)
    connectorRepository.addConnectionInfo(COMPRESSED, compressedInfo)

    DefaultTableCopyTool(connectorRepository, SOURCE, COMPRESSED).copyTables()
    val contentType = Files.probeContentType(File(FILE_HSQL).toPath())

    assertThat(contentType).isEqualTo("application/x-gzip-compressed")
  }

  @Test
  fun `Explicit encoding`() {
    val exportPlainConnectorInfo =
      ExportSQLConnectorInfo(SOURCE, DatabaseType.HSQLDB, path = FILE_HSQL, schema = "", encoding = ISO_8859_1)
    connectorRepository.addConnectionInfo(SCRIPT, exportPlainConnectorInfo)

    DefaultTableCopyTool(connectorRepository, SOURCE, SCRIPT).copyTables()

    val dataScriptUtf8 = File(FILE_HSQL).readLines(UTF_8)
    val dataScriptIso = File(FILE_HSQL).readLines(ISO_8859_1)

    assertThat(dataScriptUtf8).doesNotContain(TEST_STRING)
    assertThat(dataScriptIso).contains(TEST_STRING)
  }

  companion object {
    private const val FILE_HSQL = "./target/dump.sql"
    private const val FILE_MYSQL = "./target/dump-mysql.sql"
    private const val TEST_STRING = "	(3, 'Häagen daß', 'Y');"

    const val SCRIPT = "SCRIPT"
    const val MYSQL = "MYSQL"
    const val COMPRESSED = "COMPRESSED"
  }
}