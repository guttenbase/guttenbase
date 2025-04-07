package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.H2DB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.tools.DropTablesTool
import io.github.guttenbase.tools.InsertStatementTool
import io.github.guttenbase.tools.ReadTableDataTool
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.sql.SQLIntegrityConstraintViolationException

/**
 * Drop schema information and data
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DropTablesToolTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(H2DB, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())

    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/ddl/tables-hsqldb.sql")
    scriptExecutorTool.executeFileScript(H2DB, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(DERBYDB, resourceName = "/ddl/tables-derby.sql")
    scriptExecutorTool.executeFileScript(HSQLDB, false, false, "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(H2DB, false, false, "/data/test-data.sql")
    scriptExecutorTool.executeFileScript(DERBYDB, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Delete data, but keep schema`() {
    delete(HSQLDB)
    delete(H2DB)
    delete(DERBYDB)
  }

  private fun delete(target: String) {
    val data1 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)

    DropTablesTool(connectorRepository, target).clearTables()

    val data2 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data2).isEmpty()
  }

  @Test
  fun `Drop tables`() {
    drop(HSQLDB)
    drop(H2DB)
    drop(DERBYDB)
  }

  private fun drop(target: String) {
    val tool = DropTablesTool(connectorRepository, target)
    tool.dropViews()
    tool.dropTables()

    assertThat(connectorRepository.getDatabase(target).tables).isEmpty()
  }

  @Test
  fun `Drop all`() {
    dropAll(HSQLDB)
    dropAll(H2DB)
    dropAll(DERBYDB)
  }

  private fun dropAll(target: String) {
    DropTablesTool(connectorRepository, target).dropAll()

    assertThat(connectorRepository.getDatabase(target).tables).isEmpty()
  }

  @Test
  fun `Drop constraints`() {
    dropConstraints(HSQLDB)
    dropConstraints(H2DB)
    dropConstraints(DERBYDB)
  }

  private fun dropConstraints(target: String) {
    assertThrows<SQLIntegrityConstraintViolationException> {
      InsertStatementTool(connectorRepository, target).createInsertStatement("FOO_USER_COMPANY")
        .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
        .execute()
    }

    DropTablesTool(connectorRepository, target).dropForeignKeys()

    InsertStatementTool(connectorRepository, target).createInsertStatement("FOO_USER_COMPANY")
      .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
      .execute()
  }

  @Test
  fun `Drop indexes`() {
    dropIndexes(H2DB)
    dropIndexes(HSQLDB)
    dropIndexes(DERBYDB)
  }

  private fun dropIndexes(target: String) {
    DropTablesTool(connectorRepository, target).dropIndexes()

    val data1 = ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)
  }
}