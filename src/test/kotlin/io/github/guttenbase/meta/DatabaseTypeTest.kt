package io.github.guttenbase.io.github.guttenbase.meta

import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.impl.ColumnMetaDataImpl
import io.github.guttenbase.meta.impl.DatabaseMetaDataImpl
import io.github.guttenbase.meta.impl.TableMetaDataImpl
import io.github.guttenbase.repository.ConnectorRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.sql.Types

class DatabaseTypeTest {
  val connectorRepository = ConnectorRepository().apply {
    addConnectionInfo("jens", TestH2ConnectionInfo())
  }
  private val table =
    TableMetaDataImpl(
      DatabaseMetaDataImpl(connectorRepository, "jens", "dbo", HashMap(), DatabaseType.MOCK),
      "TEST",
      "TABLE",
      "",
      ""
    )
  private val column = ColumnMetaDataImpl(table, Types.BIGINT, "ID", "BIGINT", "INTEGER", false, true, 12, 12)

  @Test
  fun `Blob clauses`() {
    assertEquals("CAST (X'", DatabaseType.H2DB.getBlobDataPrefix())
    assertEquals("0x", DatabaseType.MSSQL.getBlobDataPrefix())
    assertEquals("' AS BLOB)", DatabaseType.H2DB.getBlobDataSuffix())
    assertEquals("", DatabaseType.MSSQL.getBlobDataSuffix())
  }

  @Test
  fun `Auto-increment clause`() {
    assertEquals("GENERATED BY DEFAULT AS IDENTITY", DatabaseType.H2DB.createColumnAutoincrementClause(column))
    assertEquals("IDENTITY(1, 1)", DatabaseType.MSSQL.createColumnAutoincrementClause(column))
  }
}