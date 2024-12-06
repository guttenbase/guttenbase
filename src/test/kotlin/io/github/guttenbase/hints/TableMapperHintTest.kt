package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.tools.ReadTableDataTool
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where tables have been renamed, because the source tables contain umlauts which are not always supported...
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TableMapperHintTest : AbstractHintTest(
  "/ddl/tables-with-umlauts.sql", "/ddl/tables-hsqldb.sql", "/data/test-data-with-umlauts.sql"
) {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(TARGET, object : TableMapperHint() {
      override val value: TableMapper
        get() = TestTableMapper()
    })
  }

  override fun executeChecks() {
    ReadTableDataTool(connectorRepository, TARGET, "FOO_COMPANY").start().use { tool ->
      val data = tool.readTableData(-1).sortedBy { it["NAME"].toString() }

      assertThat(data).hasSize(4)

      val last = data.last()
      assertThat(last).hasSize(3)
      assertThat(last["NAME"]).isEqualTo("Häagen daß")
    }
  }
}
