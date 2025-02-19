package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBYDB
import io.github.guttenbase.hints.H2DB
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.meta.BooleanValue
import io.github.guttenbase.meta.IntValue
import io.github.guttenbase.meta.StringValue
import io.github.guttenbase.meta.impl.ValueType
import io.github.guttenbase.serialization.JSON
import io.github.guttenbase.tools.DatabaseMetaDataExporterTool
import io.github.guttenbase.tools.DatabaseMetaDataExporterTool.Companion.importDataBaseMetaData
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.io.File

class DatabaseMetaDataExporterToolTest : AbstractGuttenBaseTest() {
  @Test
  fun `Read & Write polymorphic type`() {
    val property1 = DatabaseProperty("maxColumnNameLength", IntValue(12))
    val property2 = DatabaseProperty("vendorName", StringValue("Jens"))
    val property3 = DatabaseProperty("supportsFeature", BooleanValue(true))

    listOf<DatabaseProperty>(property1, property2, property3)

    val asJSON = JSON.encodeToString(listOf<DatabaseProperty>(property1, property2, property3)).also {
      assertThat(it).contains(""""propertyName": "maxColumnNameLength"""").contains(""""value": 12""")
    }

    val databaseProperties = JSON.decodeFromString<List<DatabaseProperty>>(asJSON)
    assertThat(databaseProperties).contains(property1, property2, property3)
  }

  @Test
  fun `Export & Import Database MetaData`() {
    setup()

    test(HSQLDB, "HSQL Database Engine Driver")
    test(DERBYDB, "Apache Derby Embedded JDBC Driver")
    test(H2DB, "H2 JDBC Driver")
  }

  private fun test(connectorId: String, expectedDriverName: String) {
    val file = File.createTempFile(connectorId, ".json").apply { deleteOnExit() }

    DatabaseMetaDataExporterTool(connectorRepository, connectorId).export(file)

    val metaData1 = connectorRepository.getDatabase(connectorId)
    val metaData2 = importDataBaseMetaData(file, connectorId, connectorRepository)

    assertThat(metaData2.metaData.driverName).isEqualTo(metaData1.metaData.driverName)
      .isEqualTo(expectedDriverName)
    assertThat(metaData2.supportedTypes).isEqualTo(metaData1.supportedTypes)
    assertThat(metaData2.tables).isEqualTo(metaData1.tables)

    metaData2.getTable("FOO_COMPANY")!!.exportedForeignKeys.forEach { fk ->
      val referencedTable2 = fk.referencedTable
      val referencedTable1 = metaData1.getTable(referencedTable2.tableName)!!
      val referencingTable2 = fk.referencingTable
      val referencedColumn = fk.referencedColumns[0]
      val referencingColumn = fk.referencingColumns[0]

      assertThat(referencedTable2.syntheticId).isEqualTo(referencedTable1.syntheticId)

      assertThat(referencedColumn).isSameAs(referencedTable2.getColumn(referencedColumn.columnName))
      assertThat(referencingColumn).isSameAs(referencingTable2.getColumn(referencingColumn.columnName))
      assertThat(referencingColumn).isNotEqualTo(referencedColumn)

      assertThat(referencingColumn.table).isEqualTo(metaData1.getTable(referencingColumn.table.tableName))
      assertThat(referencingColumn).isEqualTo(
        metaData1.getTable(referencingColumn.table.tableName)!!.getColumn(referencingColumn.columnName)
      )
    }
  }

  private fun setup() {
    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(H2DB, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(DERBYDB, TestDerbyConnectionInfo())

    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/ddl/tables-hsqldb.sql")
    scriptExecutorTool.executeFileScript(H2DB, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(DERBYDB, resourceName = "/ddl/tables-derby.sql")
  }
}

@Serializable
data class DatabaseProperty(val propertyName: String, val value: ValueType)
