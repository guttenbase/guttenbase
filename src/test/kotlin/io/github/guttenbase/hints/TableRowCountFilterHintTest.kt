package io.github.guttenbase.hints

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.TableRowCountFilter
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach

/**
 * Test omitting row count statement
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class TableRowCountFilterHintTest :
  AbstractHintTest("/ddl/tables-derby.sql", "/ddl/tables-hsqldb.sql", "/data/test-data.sql") {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectorHint(SOURCE, object : TableRowCountFilterHint() {
      override val value: TableRowCountFilter
        get() = object : TableRowCountFilter {
          override fun accept(tableMetaData: TableMetaData) = false

          override fun defaultRowCount(tableMetaData: TableMetaData) =
            if (tableMetaData.tableName.equals("FOO_DATA", ignoreCase = true)) 0 else 1

          override fun defaultMaxId(tableMetaData: TableMetaData) = 7L

          override fun defaultMinId(tableMetaData: TableMetaData) = 3L
        }
    })
  }

  override fun executeChecks() {
    val source = connectorRepository.getDatabase(SOURCE).getTable("FOO_USER")!!
    val target = connectorRepository.getDatabase(TARGET).getTable("FOO_USER")!!

    assertEquals(1, source.filteredRowCount)
    assertEquals(1, target.filteredRowCount)
    assertEquals(7L, source.maxId)
    assertEquals(3L, source.minId)
  }
}
