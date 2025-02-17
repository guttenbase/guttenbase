@file:Suppress("JavaIoSerializableObjectMustHaveReadResolve")

package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.connector.impl.PropertiesEncryptionTool
import io.github.guttenbase.connector.impl.PropertiesURLConnectorInfo
import io.github.guttenbase.connector.impl.URLConnectorInfo
import io.github.guttenbase.connector.impl.WrapperConnectorInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.H2DB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.hints.SOURCE
import io.github.guttenbase.hints.impl.LoggingScriptExecutorProgressIndicatorHint
import io.github.guttenbase.hints.impl.LoggingTableCopyProgressIndicatorHint
import io.github.guttenbase.meta.DatabaseType.POSTGRESQL
import io.github.guttenbase.schema.CopySchemaTool
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.InsertStatementTool
import io.github.guttenbase.tools.ReadTableDataTool
import io.github.guttenbase.tools.ScriptExecutorTool
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


/**
 * Copy schema between databases
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class CopySchemaToolTest : AbstractGuttenBaseTest() {

  @BeforeEach
  fun setupTables() {
    val stream = CopySchemaToolTest::class.java.getResourceAsStream("/hsqldb.properties")
    val encrypted = CopySchemaToolTest::class.java.getResourceAsStream("/encrypted.properties")
    val decryptedProperties = PropertiesEncryptionTool(encrypted!!).decrypt("guttenbase")

    connectorRepository.addConnectionInfo(SOURCE, TestH2ConnectionInfo())
      .addConnectionInfo(H2DB, TestH2ConnectionInfo()).addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())
      .addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
      .addConnectionInfo(PROPS, PropertiesURLConnectorInfo(stream!!))
      .addConnectionInfo(ENCRYPTED, PropertiesURLConnectorInfo(decryptedProperties))
      .addConnectionInfo(POSTGRES, TestPostgresqlConnectionInfo)
      .addConnectorHint(null, LoggingTableCopyProgressIndicatorHint)
      .addConnectorHint(null, LoggingScriptExecutorProgressIndicatorHint)

    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables-h2.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/data/test-data.sql")
  }

  @Test
  fun testDerby() {
    test(DERBYDB)
  }

  @Test
  fun testHSQLDB() {
    test(HSQLDB)
  }

  @Test
  fun testH2() {
    test(H2DB)
  }

  @Test
  fun testPostgres() {
    test(POSTGRES)
  }

  @Test
  fun testPropertiesConnectionInfo() {
    test(PROPS)
  }

  @Test
  fun testEncryptedPropertiesConnectionInfo() {
    val connectionInfo1 = connectorRepository.getConnectionInfo(PROPS) as URLConnectorInfo
    val connectionInfo2 = connectorRepository.getConnectionInfo(ENCRYPTED) as URLConnectorInfo

    assertThat(connectionInfo1.driver).isEqualTo(connectionInfo2.driver)

    test(ENCRYPTED)
  }

  private fun test(target: String) {
    CopySchemaTool(connectorRepository, SOURCE, target).copySchema()
    DefaultTableCopyTool(connectorRepository, SOURCE, target).copyTables()

    val databaseMetaData = connectorRepository.getDatabase(target)
    val tableMetaData = databaseMetaData.getTable("FOO_COMPANY")!!

    assertThat(tableMetaData.totalRowCount).isEqualTo((4))
    assertThat(tableMetaData.filteredRowCount).isEqualTo((4))
    assertThat(tableMetaData.minId).isEqualTo((1))
    assertThat(tableMetaData.maxId).isEqualTo((4))

    // Explicit ID
    InsertStatementTool(connectorRepository, target).createInsertStatement(
      "FOO_COMPANY", includePrimaryKey = true
    ).setParameter("SUPPLIER", 'x').setParameter("NAME", "JENS")
      .setParameter("ID", 0L)
      .execute()

    // Implicit ID
    InsertStatementTool(connectorRepository, target).createInsertStatement(
      "FOO_COMPANY", includePrimaryKey = false
    ).setParameter("SUPPLIER", 'x').setParameter("NAME", "HIPPE")
      .execute()

    ReadTableDataTool(connectorRepository, target, "FOO_COMPANY").start().use { tool ->
      val data = tool.readTableData(-1).sortedBy { it["ID"].toString().toInt() }

      assertThat(data).hasSize(6)
      val first = data.first()
      assertThat(first).hasSize(3)
      assertThat(first["NAME"]).isEqualTo("JENS")
      assertThat(first["ID"]).isEqualTo(0L)

      val last = data.last()
      assertThat(last).hasSize(3)
      assertThat(last["NAME"]).isEqualTo("HIPPE")
      assertThat(last["ID"] as Long).isGreaterThanOrEqualTo(5L)
    }
  }

  companion object {
    const val PROPS = "PROPS"
    const val ENCRYPTED = "ENCRYPTED"
    const val POSTGRES = "POSTGRES"

    @JvmStatic
    private val embeddedPostgres: EmbeddedPostgres by lazy { EmbeddedPostgres.start() }

    @JvmStatic
    @AfterAll
    fun teardown() {
      embeddedPostgres.close()
    }

    object TestPostgresqlConnectionInfo : WrapperConnectorInfo("", POSTGRESQL, { embeddedPostgres.postgresDatabase.connection })
  }
}
