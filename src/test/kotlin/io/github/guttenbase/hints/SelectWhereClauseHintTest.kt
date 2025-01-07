package io.github.guttenbase.hints

import io.github.guttenbase.tools.SelectWhereClause
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

/**
 * Test a schema migration wheer data will be omited using a WHERE clause
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class SelectWhereClauseHintTest : AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-hsqldb.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : SelectWhereClauseHint() {
      override val value: SelectWhereClause
        get() = SelectWhereClause {
          when (it.tableName) {
            "FOO_USER" -> "WHERE ID <= 3"
            "FOO_USER_COMPANY", "FOO_USER_ROLES" -> "WHERE USER_ID <= 3"
            else -> ""
          }
        }
    })
  }

  override fun executeChecks() {
    val source = connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")!!
    val target = connectorRepository.getDatabaseMetaData(TARGET).getTableMetaData("FOO_USER")!!
    assertEquals(4, source.totalRowCount)
    assertEquals(3, source.filteredRowCount)
    assertEquals(3, target.totalRowCount)
    assertEquals(3, target.filteredRowCount)
  }
}
