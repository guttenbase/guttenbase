package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.DERBY
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.H2
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.HSQLDB
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

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
    val h2 = connectorRepository.getDatabaseMetaData(H2)
    assertEquals("H2 JDBC Driver", h2.databaseMetaData.driverName)
    val hsqldb = connectorRepository.getDatabaseMetaData(HSQLDB)
    assertEquals("HSQL Database Engine Driver", hsqldb.databaseMetaData.driverName)
  }
}