package io.github.guttenbase

import io.github.guttenbase.configuration.DB_DIRECTORY
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream

@Suppress("SqlNoDataSourceInspection")
abstract class AbstractGuttenBaseTest {
  protected val connectorRepository: ConnectorRepository = ConnectorRepository()

  @BeforeEach
  fun dropTables() {
    Util.deleteDirectory(DB_DIRECTORY)
  }
}
