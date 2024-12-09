package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.DERBY
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.H2
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.HSQLDB
import io.github.guttenbase.meta.DatabaseSupportedType
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType.BIGINT
import java.sql.JDBCType.BLOB
import java.sql.JDBCType.VARCHAR

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
    assertEquals("Apache Derby Embedded JDBC Driver", derby.databaseMetaData.driverName)
    assertThat(derby.supportedTypes).contains(
      DatabaseSupportedType("VARCHAR", VARCHAR, 32672, true),
      DatabaseSupportedType("BIGINT", BIGINT, 19, true),
      DatabaseSupportedType("BLOB", BLOB, 2147483647, true)
    )
    assertThat(derby.typeFor(VARCHAR)).extracting { it?.typeName }.isEqualTo("VARCHAR")
    assertThat(derby.typeFor(BLOB)).extracting { it?.typeName }.isEqualTo("BLOB")
    assertThat(derby.typeFor(BIGINT)).extracting { it?.typeName }.isEqualTo("BIGINT")

    val h2 = connectorRepository.getDatabaseMetaData(H2)
    assertEquals("H2 JDBC Driver", h2.databaseMetaData.driverName)
    assertThat(h2.supportedTypes).contains(
      DatabaseSupportedType("CHARACTER VARYING", VARCHAR, 1000000000, true),
      DatabaseSupportedType("BIGINT", BIGINT, 64, true),
      DatabaseSupportedType("BINARY LARGE OBJECT", BLOB, 2147483647, true)
    )
    assertThat(h2.typeFor(VARCHAR)).extracting { it?.typeName }.isEqualTo("CHARACTER VARYING")
    assertThat(h2.typeFor(BLOB)).extracting { it?.typeName }.isEqualTo("BINARY LARGE OBJECT")
    assertThat(h2.typeFor(BIGINT)).extracting { it?.typeName }.isEqualTo("BIGINT")

    val hsqldb = connectorRepository.getDatabaseMetaData(HSQLDB)
    assertEquals("HSQL Database Engine Driver", hsqldb.databaseMetaData.driverName)
    assertThat(hsqldb.supportedTypes).contains(
      DatabaseSupportedType("VARCHAR", VARCHAR, 2147483647, true),
      DatabaseSupportedType("BIGINT", BIGINT, 64, true),
      DatabaseSupportedType("BLOB", BLOB, 2147483647, true)
    )
    assertThat(hsqldb.typeFor(VARCHAR)).extracting { it?.typeName }.isEqualTo("NVARCHAR")
    assertThat(hsqldb.typeFor(BLOB)).extracting { it?.typeName }.isEqualTo("BLOB")
    assertThat(hsqldb.typeFor(BIGINT)).extracting { it?.typeName }.isEqualTo("BIGINT")
  }
}