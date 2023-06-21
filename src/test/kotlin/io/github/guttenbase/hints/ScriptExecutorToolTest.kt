package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.util.*

/**
 * Execute updates on schema
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ScriptExecutorToolTest : AbstractGuttenBaseTest() {
  private val objectUnderTest = ScriptExecutorTool(connectorRepository)

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    objectUnderTest.executeFileScript(TARGET, resourceName = "/ddl/tables.sql")
    objectUnderTest.executeFileScript(TARGET, false, false, "/data/test-data.sql")
  }

  @Test
  fun testAction() {
    val nulls1: List<Any?> = objectUnderTest.executeQuery(TARGET, "SELECT * FROM FOO_USER")
      .map { it["PERSONAL_NUMBER"] }.filter { Objects.isNull(it) }

    assertFalse(nulls1.isEmpty())

    objectUnderTest.executeQuery(
      TARGET, "SELECT * FROM FOO_USER",
      object : ScriptExecutorTool.StatementCommand("UPDATE FOO_USER SET PERSONAL_NUMBER = ?  WHERE ID = ?") {
        override fun execute(connection: Connection, data: Map<String, Any>) {
          if (data["PERSONAL_NUMBER"] == null) {
            val id = data["ID"] as Long

            statement.setInt(1, 4711)
            statement.setLong(2, id)
            statement.execute()
          }
        }
      })

    val nulls2: List<Any?> = objectUnderTest.executeQuery(TARGET, "SELECT * FROM FOO_USER")
      .map { it["PERSONAL_NUMBER"] }.filter { Objects.isNull(it) }
    assertTrue(nulls2.isEmpty())
  }
}