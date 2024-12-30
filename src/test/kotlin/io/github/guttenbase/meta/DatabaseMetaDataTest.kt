package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBY
import io.github.guttenbase.hints.H2
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.meta.DatabaseSupportedColumnType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType.*

class DatabaseMetaDataTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(DERBY, TestDerbyConnectionInfo())
      .addConnectionInfo(H2, TestH2ConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
  }

  @Test
  fun `Inspect database meta data`() {
    val derby = connectorRepository.getDatabaseMetaData(DERBY)
    val derbyVarchar = DatabaseSupportedColumnType("VARCHAR", VARCHAR, 32672, 0, true)

    assertEquals("Apache Derby Embedded JDBC Driver", derby.databaseMetaData.driverName)
    assertThat(derby.supportedTypes.values.flatten()).contains(
      derbyVarchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 19, 0, true),
      DatabaseSupportedColumnType("BLOB", BLOB, 2147483647, 0, true)
    )
    assertThat(derby.supportedTypes[VARCHAR]).containsOnly(derbyVarchar)

    val h2 = connectorRepository.getDatabaseMetaData(H2)
    val h2Varchar = DatabaseSupportedColumnType("CHARACTER VARYING", VARCHAR, 1000000000, 0, true)

    assertEquals("H2 JDBC Driver", h2.databaseMetaData.driverName)
    assertThat(h2.supportedTypes.values.flatten()).contains(
      h2Varchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 64, 0, true),
      DatabaseSupportedColumnType("BINARY LARGE OBJECT", BLOB, 2147483647, 0, true)
    )
    assertThat(h2.supportedTypes[VARCHAR]).contains(h2Varchar)

    val hsqldb = connectorRepository.getDatabaseMetaData(HSQLDB)
    val hsqldbVarchar = DatabaseSupportedColumnType("VARCHAR", VARCHAR, 2147483647, 0, true)

    assertEquals("HSQL Database Engine Driver", hsqldb.databaseMetaData.driverName)
    assertThat(hsqldb.supportedTypes.values.flatten()).contains(
      hsqldbVarchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 64, 0, true),
      DatabaseSupportedColumnType("BLOB", BLOB, 2147483647, 0, true)
    )
    assertThat(hsqldb.supportedTypes[VARCHAR]).contains(hsqldbVarchar)
  }
}