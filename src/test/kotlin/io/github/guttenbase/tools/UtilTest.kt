package io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.utils.Util
import io.github.guttenbase.utils.Util.abbreviate
import io.github.guttenbase.utils.Util.forEach
import io.github.guttenbase.utils.Util.toHex
import io.github.guttenbase.utils.Util.trim
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream

internal class UtilTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    val scriptExecutorTool = ScriptExecutorTool(connectorRepository, encoding = Charsets.UTF_8)
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

  @Test
  fun `Hexa-decimal bytes`() {
    val bytes = byteArrayOf(SPACE, A)

    assertEquals("20", bytes[0].toHex())
    assertEquals("2041", bytes.toHex())
  }

  @Test
  fun `For each byte`() {
    val bytes = ByteArray(DEFAULT_BUFFER_SIZE)
    for (i in 0 until DEFAULT_BUFFER_SIZE) {
      bytes[i] = if (i % 2 == 0) SPACE else A
    }

    var count = 0
    ByteArrayInputStream(bytes).forEach {
      val ch = if (count++ % 2 == 0) SPACE else A
      assertEquals(ch, it)
    }
  }

  @Test
  fun `String helpers`() {
    assertThat(trim(null)).isEqualTo("")
    assertThat(trim("")).isEqualTo("")
    assertThat(trim("Jens ")).isEqualTo("Jens")

    assertThat("More than 15 characters".abbreviate(15)).isEqualTo("More than 15...")
  }
}

private const val SPACE = ' '.code.toByte()
private const val A = 'A'.code.toByte()
