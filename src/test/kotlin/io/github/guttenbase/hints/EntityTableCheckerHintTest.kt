package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.tools.EntityTableChecker
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class EntityTableCheckerHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(CONNECTOR_ID, TestDerbyConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(CONNECTOR_ID, resourceName = "/ddl/tables-derby.sql")
  }

  @Test
  fun testMainTable() {
    val objectUnderTest = connectorRepository.getConnectorHint(CONNECTOR_ID, EntityTableChecker::class.java).value
    assertTrue(
      objectUnderTest.isEntityTable(
        connectorRepository.getDatabaseMetaData(CONNECTOR_ID).getTableMetaData("FOO_COMPANY")!!
      )
    )
    assertTrue(
      objectUnderTest.isEntityTable(
        connectorRepository.getDatabaseMetaData(CONNECTOR_ID).getTableMetaData("FOO_USER")!!
      )
    )
    assertFalse(
      objectUnderTest.isEntityTable(
        connectorRepository.getDatabaseMetaData(CONNECTOR_ID).getTableMetaData("FOO_USER_COMPANY")!!
      )
    )
  }

  companion object {
    const val CONNECTOR_ID = "derby"
  }
}
