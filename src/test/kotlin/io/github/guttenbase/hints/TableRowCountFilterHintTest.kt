package io.github.guttenbase.hints

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.TableRowCountFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

/**
 * Test omitting row count statement
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class TableRowCountFilterHintTest : AbstractHintTest("/ddl/tables.sql", "/ddl/tables.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : TableRowCountFilterHint() {
      override val value: TableRowCountFilter
        get() = object : TableRowCountFilter {
          override fun accept(tableMetaData: TableMetaData) = false

          override fun defaultRowCount(tableMetaData: TableMetaData) =
            if (tableMetaData.tableName.equals("FOO_DATA", ignoreCase = true)) 0 else 1
        }
    })
  }

  override fun executeChecks() {
    val source =      connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")!!
    val target =      connectorRepository.getDatabaseMetaData(TARGET).getTableMetaData("FOO_USER")!!
    assertEquals(1, source.filteredRowCount)
    assertEquals(1, target.filteredRowCount)
  }
}
