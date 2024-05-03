package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.export.ExportDumpConnectorInfo
import io.github.guttenbase.export.ImportDumpConnectionInfo
import io.github.guttenbase.tools.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

private const val ZIP_FILE = "./target/dump.zip"

/**
 * Create a dump of the source database
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(EXPORT, ExportDumpConnectorInfo(SOURCE, ZIP_FILE))
    connectorRepository.addConnectionInfo(IMPORT, ImportDumpConnectionInfo(File(ZIP_FILE).toURI().toURL()))

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-derby.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(TARGET, resourceName = "/ddl/tables-h2.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Export and Import data from file`() {
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, EXPORT)

    val file = File(ZIP_FILE)

    assertTrue(file.exists())
    assertTrue(file.length() > 100)

    DefaultTableCopyTool(connectorRepository).copyTables(IMPORT, TARGET)

    CheckEqualTableDataTool(connectorRepository).checkTableData(SOURCE, TARGET)

    file.delete()
  }

  companion object {
    const val SOURCE = "SOURCE"
    const val IMPORT = "IMPORT"
    const val EXPORT = "EXPORT"
    const val TARGET = "TARGET"
  }
}
