package io.github.guttenbase.configuration

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.impl.URLConnectorInfoImpl
import org.mockito.Mockito
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

@Suppress("unused")
class MockConnectionInfo : URLConnectorInfoImpl(
  "anything", "anything", "anything", "io.github.guttenbase.configuration.MockDriver", "",
  DatabaseType.MOCK
) {
  val connection = Mockito.mock(Connection::class.java)!!
  val preparedStatement = Mockito.mock(PreparedStatement::class.java)!!
  val resultSet = Mockito.mock(ResultSet::class.java, Mockito.RETURNS_MOCKS)!!

  companion object {
    private const val serialVersionUID = 1L
  }
}
