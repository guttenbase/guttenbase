package io.github.guttenbase

import io.github.guttenbase.configuration.DB_DIRECTORY
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.impl.ConnectorRepositoryImpl
import io.github.guttenbase.utils.Util
import org.apache.commons.io.IOUtils
import org.junit.jupiter.api.BeforeEach
import java.io.ByteArrayInputStream

@Suppress("SqlNoDataSourceInspection")
abstract class AbstractGuttenBaseTest {
  protected val connectorRepository: ConnectorRepository = ConnectorRepositoryImpl()

  @BeforeEach
  fun dropTables() {
    Util.deleteDirectory(DB_DIRECTORY)
  }

  protected fun insertBinaryData(connectorId: String, dataId: Int) {
    val connector = connectorRepository.createConnector(connectorId)
    val connection = connector.openConnection()
    val preparedStatement = connection.prepareStatement("INSERT INTO FOO_DATA (ID, SOME_DATA) VALUES(?, ?)")

    preparedStatement.setLong(1, dataId.toLong())
    preparedStatement.setBinaryStream(2, ByteArrayInputStream(IMAGE))
    preparedStatement.executeUpdate()
    connector.closeConnection()
  }

  companion object {
    /**
     * Place all DB data in temporary directory. Pure in-memory DBs are faster but mess up when running multiple tests.
     */
    val IMAGE = loadImage()

    private fun loadImage(): ByteArray {
      val stream = Util.getResourceAsStream("/data/test.gif")!!
      return IOUtils.readFully(stream, stream.available())
    }
  }
}
