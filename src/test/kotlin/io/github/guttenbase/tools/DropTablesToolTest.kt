package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.tools.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Drop schema information and data
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DropTablesToolTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    val scriptExecutorTool = ScriptExecutorTool(connectorRepository)

    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(H2, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(DERBY, TestDerbyConnectionInfo())

    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/ddl/tables-hsqldb.sql")
    scriptExecutorTool.executeFileScript(H2, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(DERBY, resourceName = "/ddl/tables-derby.sql")
    scriptExecutorTool.executeFileScript(HSQLDB, false, false, "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(H2, false, false, "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(DERBY, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Delete data, but keep schema`() {
    delete(HSQLDB)
    delete(H2)
    delete(DERBY)
  }

  private fun delete(target: String) {
    val data1 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)

    DropTablesTool(connectorRepository).clearTables(target)

    val data2 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data2).isEmpty()
  }

  @Test
  fun `Drop tables`() {
    drop(HSQLDB)
    drop(H2)
    drop(DERBY)
  }

  private fun drop(target: String) {
    DropTablesTool(connectorRepository).dropTables(target)

    assertThat(connectorRepository.getDatabaseMetaData(target).tableMetaData).isEmpty()
  }

  @Test
  fun `Drop all`() {
    dropALl(HSQLDB)
    dropALl(H2)
    dropALl(DERBY)
  }

  private fun dropALl(target: String) {
    DropTablesTool(connectorRepository).dropAll(target)

    assertThat(connectorRepository.getDatabaseMetaData(target).tableMetaData).isEmpty()
  }

  @Test
  fun `Drop constraints`() {
    dropConstraints(HSQLDB)
    dropConstraints(H2)
    dropConstraints(DERBY)
  }

  private fun dropConstraints(target: String) {
    assertThrows<SQLIntegrityConstraintViolationException> {
      InsertStatementTool(connectorRepository, target).createInsertStatement("FOO_USER_COMPANY")
        .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
        .execute()
    }

    DropTablesTool(connectorRepository).dropForeignKeys(target)

    InsertStatementTool(connectorRepository, target).createInsertStatement("FOO_USER_COMPANY")
      .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
      .execute()
  }

  @Test
  fun `Drop indexes`() {
    dropIndexes(H2)
    dropIndexes(HSQLDB)
    dropIndexes(DERBY)
  }

  private fun dropIndexes(target: String) {
    DropTablesTool(connectorRepository).dropIndexes(target)

    val data1 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)
  }

  companion object {
    const val HSQLDB = "HSQLDB"
    const val H2 = "H2"
    const val DERBY = "DERBY"
  }
}