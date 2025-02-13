package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.impl.LoggingScriptExecutorProgressIndicatorHint
import io.github.guttenbase.hints.impl.LoggingTableCopyProgressIndicatorHint
import io.github.guttenbase.schema.comparison.SchemaComparatorTool
import io.github.guttenbase.tools.CheckEqualTableDataTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.ScriptExecutorTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Super class for Hint tests
 *
 * &copy; 2012-2044 akquinet tech@spree
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
      .addConnectionInfo(TARGET, TestHsqlConnectionInfo())

    // Prefer logging for tests
    connectorRepository.addConnectorHint(SOURCE, LoggingTableCopyProgressIndicatorHint)
      .addConnectorHint(SOURCE, LoggingScriptExecutorProgressIndicatorHint)
      .addConnectorHint(TARGET, LoggingTableCopyProgressIndicatorHint)
      .addConnectorHint(TARGET, LoggingScriptExecutorProgressIndicatorHint)

    val scriptExecutorTool = ScriptExecutorTool(connectorRepository, encoding = Charsets.UTF_8)
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = sourceTableSchemaScript)
    scriptExecutorTool.executeFileScript(TARGET, resourceName = targetTableSchemaScript)
    scriptExecutorTool.executeFileScript(SOURCE, false, false, dataScript)
  }

  @Test
  fun testTableCopy() {
    val issues = SchemaComparatorTool(connectorRepository, SOURCE, TARGET).check()

    assertThat(issues.isSevere).`as`(issues.toString()).isFalse()
    DefaultTableCopyTool(connectorRepository, SOURCE, TARGET).copyTables()
    CheckEqualTableDataTool(connectorRepository, SOURCE, TARGET).checkTableData()
    executeChecks()
  }

  protected open fun executeChecks() {
  }
}

const val DERBYDB = "DERBY"
const val H2DB = "H2DB"
const val HSQLDB = "HSQLDB"

const val SOURCE = "SOURCE"
const val TARGET = "TARGET"
