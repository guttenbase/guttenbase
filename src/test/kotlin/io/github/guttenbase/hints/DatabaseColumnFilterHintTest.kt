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
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
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
    val tableMetaData = connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")
    assertNotNull(tableMetaData)
    assertNotNull(tableMetaData!!.getColumnMetaData("USERNAME"))
    assertNotNull(tableMetaData.getColumnMetaData("PASSWORD"))
  }

  @Test
  fun testFilter() {
    connectorRepository.addConnectorHint(SOURCE, object : DatabaseColumnFilterHint() {
      override val value: DatabaseColumnFilter
        get() = DatabaseColumnFilter { columnMetaData ->
          columnMetaData.tableMetaData.tableName != "FOO_USER" || columnMetaData.columnName != "PASSWORD"
        }
    })
    val tableMetaData = connectorRepository.getDatabaseMetaData(SOURCE).getTableMetaData("FOO_USER")
    assertNotNull(tableMetaData)
    assertNotNull(tableMetaData!!.getColumnMetaData("USERNAME"))
    assertNull(tableMetaData.getColumnMetaData("PASSWORD"))
  }

  companion object {
    const val SOURCE = "SOURCE"
  }
}
