package io.github.guttenbase.hints

import io.github.guttenbase.mapping.ColumnOrderComparatorFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Comparator.comparingInt

/**
 * Test a schema migration where column ordering is customized...
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ColumnOrderHintTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-hsqldb.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : ColumnOrderHint() {
      override val value: ColumnOrderComparatorFactory
        get() = ColumnOrderComparatorFactory { comparingInt { it.hashCode() } }
    })
  }

  @Test
  fun `By default, PK column comes first`() {
    val tableMetaData = connectorRepository.getDatabase(TARGET).getTable("FOO_USER")!!
    val columns = ColumnOrderHint.getSortedColumns(connectorRepository, tableMetaData)

    assertThat(columns).hasSize(7).extracting<String> { it.columnName }
      .containsExactly("ID", "COMPANY_ID", "CREATED", "NAME", "PASSWORD", "PERSONAL_NUMBER", "USERNAME")
  }
}
