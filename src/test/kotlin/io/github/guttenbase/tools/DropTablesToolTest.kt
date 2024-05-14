package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
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
    connectorRepository.addConnectionInfo(SOURCE, TestHsqlConnectionInfo())

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-hsqldb.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
  }

  @Test
  fun `Delete data, but keep schema`() {
    val data1 = ReadTableDataTool(connectorRepository, SOURCE, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)

    DropTablesTool(connectorRepository).clearTables(SOURCE)

    val data2 = ReadTableDataTool(connectorRepository, SOURCE, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data2).isEmpty()
  }

  @Test
  fun `Drop tables`() {
    DropTablesTool(connectorRepository).dropTables(SOURCE)

    assertThat(connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData).isEmpty()
  }

  @Test
  fun `Drop all`() {
    DropTablesTool(connectorRepository).dropAll(SOURCE)

    assertThat(connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData).isEmpty()
  }

  @Test
  fun `Drop constraints`() {
    assertThrows<SQLIntegrityConstraintViolationException> {
      InsertStatementTool(connectorRepository, SOURCE).createInsertStatement("FOO_USER_COMPANY")
        .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
        .execute()
    }

    DropTablesTool(connectorRepository).dropForeignKeys(SOURCE)

    InsertStatementTool(connectorRepository, SOURCE).createInsertStatement("FOO_USER_COMPANY")
      .setParameter("USER_ID", 42L).setParameter("ASSIGNED_COMPANY_ID", 4711L)
      .execute()
  }

  @Test
  fun `Drop indexes`() {
    DropTablesTool(connectorRepository).dropIndexes(SOURCE)

    val data1 = ReadTableDataTool(connectorRepository, SOURCE, "FOO_COMPANY").start().use {
      it.readTableData(-1)
    }

    assertThat(data1).hasSize(4)  }

  companion object {
    const val SOURCE = "SOURCE"
    const val TARGET = "TARGET"
  }
}