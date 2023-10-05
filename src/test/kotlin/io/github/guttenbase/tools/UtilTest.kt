package io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.utils.Util
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class UtilTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    val scriptExecutorTool = ScriptExecutorTool(connectorRepository, encoding = "UTF-8")
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-derby.sql")
    System.setProperty("sun.io.serialization.extendedDebugInfo", "true")
  }

  @Test
  fun `Copy complex object`() {
    val data = connectorRepository.getDatabaseMetaData(SOURCE)
    val result = Util.copyObject(DatabaseMetaData::class.java, data)

    assertEquals(data, result)
    assertEquals("Apache Derby Embedded JDBC Driver", result.databaseMetaData.driverName)
    assertEquals(data.databaseMetaData.driverName, result.databaseMetaData.driverName)
    assertEquals(data.tableMetaData, result.tableMetaData)

    val tableMetaData1 = data.getTableMetaData("FOO_USER")!!
    val tableMetaData2 = result.getTableMetaData("FOO_USER")!!
    assertEquals(tableMetaData1.columnMetaData, tableMetaData2.columnMetaData)

    val columnMetaData1 = tableMetaData1.getColumnMetaData("ID")!!
    val columnMetaData2 = tableMetaData2.getColumnMetaData("ID")!!
    assertEquals(columnMetaData1, columnMetaData2)
    assertThat(columnMetaData1.referencingColumns).hasSize(2)
    assertThat(columnMetaData1.referencingColumns).isEqualTo(columnMetaData2.referencingColumns)
  }
}