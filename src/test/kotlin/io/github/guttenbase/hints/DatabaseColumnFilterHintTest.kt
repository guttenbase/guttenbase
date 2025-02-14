package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.repository.DatabaseColumnFilter
import io.github.guttenbase.tools.ScriptExecutorTool
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Filters column when inquiring the data base.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DatabaseColumnFilterHintTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
  }

  @Test
  fun testDefault() {
    val tableMetaData = connectorRepository.getDatabaseMetaData(SOURCE).getTable("FOO_USER")
    assertNotNull(tableMetaData)
    assertNotNull(tableMetaData!!.getColumn("USERNAME"))
    assertNotNull(tableMetaData.getColumn("PASSWORD"))
  }

  @Test
  fun testFilter() {
    connectorRepository.addConnectorHint(SOURCE, object : DatabaseColumnFilterHint() {
      override val value: DatabaseColumnFilter
        get() = DatabaseColumnFilter { columnMetaData ->
          columnMetaData.table.tableName != "FOO_USER" || columnMetaData.columnName != "PASSWORD"
        }
    })
    val tableMetaData = connectorRepository.getDatabaseMetaData(SOURCE).getTable("FOO_USER")
    assertNotNull(tableMetaData)
    assertNotNull(tableMetaData!!.getColumn("USERNAME"))
    assertNull(tableMetaData.getColumn("PASSWORD"))
  }
}
