package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.DERBY
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.H2
import io.github.guttenbase.io.github.guttenbase.tools.CopySchemaToolTest.Companion.HSQLDB
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseSupportedType
import io.github.guttenbase.meta.TableMetaData
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.sql.JDBCType
import java.sql.JDBCType.*
import java.sql.Types

class DatabaseMetaDataTest : AbstractGuttenBaseTest() {
  private class MyColumn(override var jdbcColumnType: JDBCType) : ColumnMetaData {
    override var columnType = Types.VARCHAR

    override val columnName = "JENS"
    override val columnTypeName get() = jdbcColumnType.name
    override val columnClassName: String get() = TODO("Not yet implemented")
    override val tableMetaData: TableMetaData get() = TODO("Not yet implemented")
    override val isNullable: Boolean get() = TODO("Not yet implemented")
    override val isAutoIncrement: Boolean get() = TODO("Not yet implemented")
    override val precision: Int get() = 1
    override val scale: Int get() = TODO("Not yet implemented")
    override val isPrimaryKey: Boolean get() = false
    override val referencedColumns get() = TODO("Not yet implemented")
    override val referencingColumns get() = TODO("Not yet implemented")

    override fun compareTo(other: ColumnMetaData): Int {
      TODO("Not yet implemented")
    }
  }

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
    assertThat(derby.typeFor(MyColumn(VARCHAR))).extracting { it?.typeName }.isEqualTo("VARCHAR")
    assertThat(derby.typeFor(MyColumn(BLOB))).extracting { it?.typeName }.isEqualTo("BLOB")
    assertThat(derby.typeFor(MyColumn(BIGINT))).extracting { it?.typeName }.isEqualTo("BIGINT")

    val h2 = connectorRepository.getDatabaseMetaData(H2)
    assertEquals("H2 JDBC Driver", h2.databaseMetaData.driverName)
    assertThat(h2.supportedTypes).contains(
      DatabaseSupportedType("CHARACTER VARYING", VARCHAR, 1000000000, true),
      DatabaseSupportedType("BIGINT", BIGINT, 64, true),
      DatabaseSupportedType("BINARY LARGE OBJECT", BLOB, 2147483647, true)
    )
    assertThat(h2.typeFor(MyColumn(VARCHAR))).extracting { it?.typeName }.isEqualTo("CHARACTER VARYING")
    assertThat(h2.typeFor(MyColumn(BLOB))).extracting { it?.typeName }.isEqualTo("BINARY LARGE OBJECT")
    assertThat(h2.typeFor(MyColumn(BIGINT))).extracting { it?.typeName }.isEqualTo("BIGINT")

    val hsqldb = connectorRepository.getDatabaseMetaData(HSQLDB)
    assertEquals("HSQL Database Engine Driver", hsqldb.databaseMetaData.driverName)
    assertThat(hsqldb.supportedTypes).contains(
      DatabaseSupportedType("VARCHAR", VARCHAR, 2147483647, true),
      DatabaseSupportedType("BIGINT", BIGINT, 64, true),
      DatabaseSupportedType("BLOB", BLOB, 2147483647, true)
    )
    assertThat(hsqldb.typeFor(MyColumn(VARCHAR))).extracting { it?.typeName }.isEqualTo("VARCHAR")
    assertThat(hsqldb.typeFor(MyColumn(BLOB))).extracting { it?.typeName }.isEqualTo("BLOB")
    assertThat(hsqldb.typeFor(MyColumn(BIGINT))).extracting { it?.typeName }.isEqualTo("BIGINT")
  }
}