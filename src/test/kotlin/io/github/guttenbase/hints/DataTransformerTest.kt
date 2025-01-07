package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.tools.RESULT_LIST
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where all strings are converted.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DataTransformerTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-hsqldb.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    val columnDataMapper: ColumnDataMapper = object : ColumnDataMapper {
      override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData) =
        sourceColumnMetaData.columnName.uppercase().endsWith("NAME")

      override fun map(mapping: ColumnDataMapping, value: Any?) = value.toString() + SUFFIX
    }

    DefaultColumnDataMapperProvider.addMapping(ColumnType.CLASS_STRING, ColumnType.CLASS_STRING, columnDataMapper)
  }

  override fun executeChecks() {
    val list: RESULT_LIST = ScriptExecutorTool(connectorRepository).executeQuery(
      TARGET, "SELECT DISTINCT ID, USERNAME, NAME, PASSWORD FROM FOO_USER ORDER BY ID"
    )
    assertEquals(4, list.size)

    val name = list[0]["NAME"] as String
    val userName = list[0]["USERNAME"] as String
    val password = list[0]["PASSWORD"] as String

    assertEquals("User_1$SUFFIX", userName)
    assertEquals("User 1$SUFFIX", name)
    assertEquals("secret", password)
  }

  companion object {
    private const val SUFFIX = " (converted)"
  }
}
