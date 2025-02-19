package io.github.guttenbase

import io.github.guttenbase.configuration.DB_DIRECTORY
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import io.github.guttenbase.utils.Util
import org.junit.jupiter.api.BeforeEach
import kotlin.text.Charsets.UTF_8

@Suppress("SqlNoDataSourceInspection")
abstract class AbstractGuttenBaseTest {
  protected val connectorRepository = ConnectorRepository()
  protected val scriptExecutorTool = ScriptExecutorTool(connectorRepository, encoding = UTF_8)

  @BeforeEach
  fun dropTables() {
    Util.deleteDirectory(DB_DIRECTORY)
  }
}
