package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableMapper
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where table names contains spaces and thus need to be escaped with double quotes ("")
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class TableNameMapperHintTest :
  AbstractHintTest("/ddl/tables-with-spaces.sql", "/ddl/tables-with-spaces.sql", "/data/test-data-with-spaces.sql") {

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : TableMapperHint() {
      override val value: TableMapper
        get() = TestTableNameMapper()
    })
    connectorRepository.addConnectorHint(TARGET, object : TableMapperHint() {
      override val value: TableMapper get() = TestTableNameMapper()
    })
  }
}
