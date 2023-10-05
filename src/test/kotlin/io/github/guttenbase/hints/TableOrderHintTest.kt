package io.github.guttenbase.hints

import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration where table ordering is customized...
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TableOrderHintTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-h2.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(TARGET, RandomTableOrderHint())
  }
}
