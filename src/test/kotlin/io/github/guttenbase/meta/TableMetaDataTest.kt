package io.github.guttenbase.meta

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.meta.impl.ColumnMetaDataImpl
import io.github.guttenbase.meta.impl.DatabaseMetaDataImpl
import io.github.guttenbase.meta.impl.TableMetaDataImpl
import io.github.guttenbase.repository.ConnectorRepository
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.Types

@Suppress("BooleanLiteralArgument")
class TableMetaDataTest {
  private val objectUnderTest =
    TableMetaDataImpl(
      DatabaseMetaDataImpl(ConnectorRepository(), "jens", "dbo", HashMap(), DatabaseType.MOCK),
      "TEST",
      "TABLE",
      "",
      ""
    )
  private val column = ColumnMetaDataImpl(objectUnderTest, Types.BIGINT, "ID", "BIGINT", "INTEGER", false, true, 12, 12)

  @Test
  fun `immutable delegation`() {
    Assertions.assertThat(objectUnderTest.columnMetaData).isEmpty()
    (objectUnderTest.columnMetaData as MutableList).add(column)
    Assertions.assertThat(objectUnderTest.columnMetaData).isEmpty()
    objectUnderTest.addColumn(column)
    Assertions.assertThat(objectUnderTest.columnMetaData).containsExactly(column)
  }
}