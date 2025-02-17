package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.hints.impl.DisableMultipleValueBatchInsertionHint
import io.github.guttenbase.mapping.TableRowDataFilter
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLException

/**
 * Filter data rows
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TableRowDataFilterTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-derby.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(TARGET, resourceName = "/ddl/tables-h2.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")

    connectorRepository.addConnectorHint(TARGET, object : TableRowDataFilterHint() {
      override val value: TableRowDataFilter
        get() = TableRowDataFilter { sourceValues, _ ->
          val tableMetaData: TableMetaData = sourceValues.keys.firstOrNull()?.table ?: throw IllegalStateException()

          if (tableMetaData.tableName.equals("FOO_COMPANY", ignoreCase = true)) {
            val name = sourceValues[tableMetaData.getColumn("NAME")]!!
            return@TableRowDataFilter name != "Company 2"
          }

          true
        }
    })
  }

  @Test
  fun testExpectExeptionIfNumberOfRowsPerBatchAllowsMultipleValuesClauses() {
   assertThrows<SQLException> {  DefaultTableCopyTool(connectorRepository, SOURCE, TARGET).copyTables() }
  }

  @Test
  fun testOmitData() {
    connectorRepository.addConnectorHint(TARGET, DisableMultipleValueBatchInsertionHint)
    DefaultTableCopyTool(connectorRepository, SOURCE, TARGET).copyTables()

    val sourceTable = connectorRepository.getDatabase(SOURCE).getTable("FOO_COMPANY")!!
    val targetTable = connectorRepository.getDatabase(TARGET).getTable("FOO_COMPANY")!!

    assertEquals(4, sourceTable.totalRowCount)
    assertEquals(3, targetTable.totalRowCount)
  }
}
