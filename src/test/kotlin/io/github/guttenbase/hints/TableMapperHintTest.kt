package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where tables have been renamed, because the source tables contain umlauts which are not always supported...
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class TableMapperHintTest :
  AbstractHintTest("/ddl/tables-with-umlauts.sql", "/ddl/tables.sql", "/data/test-data-with-umlauts.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(TARGET, object : TableMapperHint() {
      override val value: TableMapper
        get() = TestTableMapper()
    })
  }
}
