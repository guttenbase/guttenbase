package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.schema.comparison.SchemaComparatorTool
import io.github.guttenbase.tools.CheckEqualTableDataTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Super class for Hint tests
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractHintTest(
  private val sourceTableSchemaScript: String,
  private val targetTableSchemaScript: String,
  private val dataScript: String
) : AbstractGuttenBaseTest() {

  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    val scriptExecutorTool = ScriptExecutorTool(connectorRepository, encoding = "UTF-8")
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = sourceTableSchemaScript)
    scriptExecutorTool.executeFileScript(TARGET, resourceName = targetTableSchemaScript)
    scriptExecutorTool.executeFileScript(SOURCE, false, false, dataScript)
  }

  @Test
  fun testTableCopy() {
    val issues = SchemaComparatorTool(connectorRepository).check(SOURCE, TARGET)
    Assertions.assertThat(issues.isSevere).`as`(issues.toString()).isFalse()
    DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, TARGET)
    CheckEqualTableDataTool(connectorRepository).checkTableData(SOURCE, TARGET)
    executeChecks()
  }

  protected open fun executeChecks() {
  }
}

const val SOURCE = "SOURCE"
const val TARGET = "TARGET"
