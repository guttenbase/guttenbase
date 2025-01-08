package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.H2
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.tools.ScriptExecutorTool
import io.github.guttenbase.tools.TableOrderTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TableOrderToolTest : AbstractGuttenBaseTest() {
  private val h2DatabaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(H2) }
  private val hsqldbDatabaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(HSQLDB) }

  @BeforeEach
  fun setupTables() {
    connectorRepository.addConnectionInfo(H2, TestH2ConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(H2, resourceName = "/ddl/departments.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(
      HSQLDB,
      resourceName = "/ddl/countries-cities-currencies.sql"
    )
  }

  @Test
  fun `Proper ordering with self-referencing table`() {
    assertThat(TableOrderTool(h2DatabaseMetaData).orderTables(topDown = true)).hasSize(2)
      .extracting<String> { it.tableName }.containsExactly("DEPARTMENTS", "EMPLOYEES")
    assertThat(TableOrderTool(h2DatabaseMetaData).orderTables(topDown = false)).hasSize(2)
      .extracting<String> { it.tableName }.containsExactly("EMPLOYEES", "DEPARTMENTS")
  }

  @Test
  fun `Proper order`() {
    assertThat(TableOrderTool(hsqldbDatabaseMetaData).orderTables(topDown = true)).hasSize(5)
      .extracting<String> { it.tableName }
      .containsExactly("REGIONS", "COUNTRIES", "CITIES", "CURRENCIES", "CURRENCIES_COUNTRIES")
    assertThat(TableOrderTool(hsqldbDatabaseMetaData).orderTables(topDown = false)).hasSize(5)
      .extracting<String> { it.tableName }
      .containsExactly("CITIES", "CURRENCIES_COUNTRIES", "COUNTRIES", "REGIONS", "CURRENCIES")
  }
}