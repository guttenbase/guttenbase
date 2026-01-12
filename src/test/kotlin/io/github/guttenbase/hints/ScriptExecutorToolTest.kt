package io.github.guttenbase.hints

import io.github.guttenbase.io.github.guttenbase.tools.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.tools.FailureMode
import io.github.guttenbase.tools.RESULT_MAP
import io.github.guttenbase.tools.ScriptExecutorTool
import io.github.guttenbase.tools.StatementCommand
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.Connection
import java.util.*

/**
 * Execute updates on schema
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class ScriptExecutorToolTest : AbstractGuttenBaseTest() {
  private val objectUnderTest = scriptExecutorTool

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(TARGET, TestHsqlConnectionInfo())
    objectUnderTest.executeFileScript(TARGET, resourceName = "/ddl/tables-hsqldb.sql")
    objectUnderTest.executeFileScript(TARGET, false, false, "/data/test-data.sql")
  }

  @Test
  fun `retry statements`() {
    val tool = ScriptExecutorTool(connectorRepository, failureMode = FailureMode.CONTINUE)
    val result1 = tool.executeScript(
      TARGET, prepareTargetConnection = false, lines = listOf(
        "INSERT INTO FOO_USER_ROLES (USER_ID, ROLE_ID) VALUES(1, 42);", // wrong order
        "INSERT INTO FOO_ROLE (ID, FIXED_ROLE, ROLE_NAME) VALUES(42, 'Y', 'ADMIN');"
      )
    )

    assertThat(result1.failedStatements()).hasSize(1)
    assertThat(result1.failedStatements()[0].second.message).contains("integrity constraint violation")

    val result2 = result1.retry(TARGET)
    assertThat(result2.failedStatements()).hasSize(0)
  }

  @Test
  fun `run statement command`() {
    val nulls1: List<Any?> = objectUnderTest.executeQuery(TARGET, "SELECT * FROM FOO_USER")
      .map { it["PERSONAL_NUMBER"] }.filter { Objects.isNull(it) }

    assertFalse(nulls1.isEmpty())

    objectUnderTest.executeQuery(
      TARGET, "SELECT * FROM FOO_USER",
      object : StatementCommand("UPDATE FOO_USER SET PERSONAL_NUMBER = ?  WHERE ID = ?") {
        override fun execute(connection: Connection, data: RESULT_MAP) {
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
