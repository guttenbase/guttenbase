package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.H2DB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.meta.DatabaseSupportedColumnType
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.copy
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType.*

class DatabaseMetaDataTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())
      .addConnectionInfo(H2DB, TestH2ConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    scriptExecutorTool.executeFileScript(DERBYDB, resourceName = "/ddl/tables-derby.sql")
  }

  @Test
  fun `Inspect database meta data`() {
    val derby = connectorRepository.getDatabase(DERBYDB)
    val derbyVarchar = DatabaseSupportedColumnType("VARCHAR", VARCHAR, 32672, 0, true)

    assertEquals("Apache Derby Embedded JDBC Driver", derby.metaData.driverName)
    assertThat(derby.supportedTypes.values.flatten()).contains(
      derbyVarchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 19, 0, true),
      DatabaseSupportedColumnType("BLOB", BLOB, 2147483647, 0, true)
    )
    assertThat(derby.supportedTypes[VARCHAR]).containsOnly(derbyVarchar)

    val h2 = connectorRepository.getDatabase(H2DB)
    val h2Varchar = DatabaseSupportedColumnType("CHARACTER VARYING", VARCHAR, 1000000000, 0, true)

    assertEquals("H2 JDBC Driver", h2.metaData.driverName)
    assertThat(h2.supportedTypes.values.flatten()).contains(
      h2Varchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 64, 0, true),
      DatabaseSupportedColumnType("BINARY LARGE OBJECT", BLOB, 2147483647, 0, true)
    )
    assertThat(h2.supportedTypes[VARCHAR]).contains(h2Varchar)

    val hsqldb = connectorRepository.getDatabase(HSQLDB)
    val hsqldbVarchar = DatabaseSupportedColumnType("VARCHAR", VARCHAR, 2147483647, 0, true)

    assertEquals("HSQL Database Engine Driver", hsqldb.metaData.driverName)
    assertThat(hsqldb.supportedTypes.values.flatten()).contains(
      hsqldbVarchar,
      DatabaseSupportedColumnType("BIGINT", BIGINT, 64, 0, true),
      DatabaseSupportedColumnType("BLOB", BLOB, 2147483647, 0, true)
    )
    assertThat(hsqldb.supportedTypes[VARCHAR]).contains(hsqldbVarchar)
  }

  @Test
  fun `Copy complex object`() {
    val data = connectorRepository.getDatabase(DERBYDB) as InternalDatabaseMetaData
    val result = data.copy()

    assertEquals(data, result)
    assertEquals("Apache Derby Embedded JDBC Driver", result.metaData.driverName)
    assertEquals(data.metaData.driverName, result.metaData.driverName)
    assertEquals(data.tables, result.tables)

    val tableMetaData1 = data.getTable("FOO_USER")!!
    val tableMetaData2 = result.getTable("FOO_USER")!!
    assertEquals(tableMetaData1.columns, tableMetaData2.columns)

    val columnMetaData1 = tableMetaData1.getColumn("ID")!!
    val columnMetaData2 = tableMetaData2.getColumn("ID")!!
    assertEquals(columnMetaData1, columnMetaData2)
    assertThat(columnMetaData1.referencingColumns).hasSize(2)
    assertThat(columnMetaData1.referencingColumns).isEqualTo(columnMetaData2.referencingColumns)
  }
}