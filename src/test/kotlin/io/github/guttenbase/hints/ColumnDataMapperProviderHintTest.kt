package io.github.guttenbase.hints

import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.hints.impl.DefaultColumnDataMapperProviderHint
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.tools.RESULT_LIST
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where all BIGINT IDs are converted to UUID strings.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ColumnDataMapperProviderHintTest : AbstractHintTest("/ddl/tables-hsqldb.sql", "/ddl/tables-uuid.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    val columnDataMapper = TestUUIDColumnDataMapper()
    connectorRepository.addConnectorHint(TARGET, object : DefaultColumnDataMapperProviderHint() {
      override fun addMappings(columnDataMapperFactory: DefaultColumnDataMapperProvider) {
        super.addMappings(columnDataMapperFactory)

        columnDataMapperFactory.addMapping(ColumnType.CLASS_LONG, ColumnType.CLASS_STRING, columnDataMapper)
        columnDataMapperFactory.addMapping(ColumnType.CLASS_BIGDECIMAL, ColumnType.CLASS_STRING, columnDataMapper)
      }
    })
  }

  override fun executeChecks() {
    val list: RESULT_LIST = ScriptExecutorTool(connectorRepository).executeQuery(
      TARGET, "SELECT DISTINCT ID FROM FOO_USER ORDER BY ID"
    )
    assertEquals(5, list.size)
    val id = list[0]["ID"]
    assertNotNull(id)

    // Given that String#hashCode is deterministic
    assertEquals("ffffffff-88e7-1891-0000-000000000001", id)
  }
}
