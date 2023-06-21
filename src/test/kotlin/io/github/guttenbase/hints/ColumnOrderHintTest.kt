package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnOrderComparatorFactory
import org.junit.jupiter.api.BeforeEach
import java.util.Comparator.comparingInt

/**
 * Test a schema migration where column ordering is customized...
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ColumnOrderHintTest : AbstractHintTest("/ddl/tables.sql", "/ddl/tables.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : ColumnOrderHint() {
      override val value: ColumnOrderComparatorFactory
        get() = ColumnOrderComparatorFactory { comparingInt { it.hashCode() } }
    })
  }
}
