package io.github.guttenbase

import io.github.guttenbase.configuration.DB_DIRECTORY
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import org.junit.jupiter.api.BeforeEach

@Suppress("SqlNoDataSourceInspection")
abstract class AbstractGuttenBaseTest {
  protected val connectorRepository = ConnectorRepository()

  @BeforeEach
  fun dropTables() {
    Util.deleteDirectory(DB_DIRECTORY)
  }
}
