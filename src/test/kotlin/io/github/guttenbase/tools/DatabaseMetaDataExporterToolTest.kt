package io.github.guttenbase.io.github.guttenbase.tools

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.configuration.TestHsqlConnectionInfo
import io.github.guttenbase.hints.DERBY
import io.github.guttenbase.hints.H2
import io.github.guttenbase.hints.HSQLDB
import io.github.guttenbase.meta.BooleanValue
import io.github.guttenbase.meta.IntValue
import io.github.guttenbase.meta.StringValue
import io.github.guttenbase.meta.impl.ValueType
import io.github.guttenbase.serialization.JSON
import io.github.guttenbase.tools.DatabaseMetaDataExporterTool
import io.github.guttenbase.tools.ScriptExecutorTool
import kotlinx.serialization.Serializable
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DatabaseMetaDataExporterToolTest : AbstractGuttenBaseTest() {
  @BeforeEach
  fun setup() {
    val scriptExecutorTool = ScriptExecutorTool(connectorRepository)

    connectorRepository.addConnectionInfo(HSQLDB, TestHsqlConnectionInfo())
    connectorRepository.addConnectionInfo(H2, TestH2ConnectionInfo())
    connectorRepository.addConnectionInfo(DERBY, TestDerbyConnectionInfo())

    scriptExecutorTool.executeFileScript(HSQLDB, resourceName = "/ddl/tables-hsqldb.sql")
    scriptExecutorTool.executeFileScript(H2, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(DERBY, resourceName = "/ddl/tables-derby.sql")
  }

  @Test
  fun `Read & Write polymorphic DatabaseProperty`() {
    val property1 = DatabaseProperty("maxColumnNameLength", IntValue(12))
    val property2 = DatabaseProperty("vendorName", StringValue("Jens"))
    val property3 = DatabaseProperty("supportsFeature", BooleanValue(true))

    listOf<DatabaseProperty>(property1, property2, property3)

    val asJSON = JSON.encodeToString(listOf<DatabaseProperty>(property1, property2, property3)).also {
      assertThat(it).contains(""""propertyName": "maxColumnNameLength"""").contains(""""value": 12""")
    }

    val databaseProperties = JSON.decodeFromString<List<DatabaseProperty>>(asJSON)
   assertThat(databaseProperties).contains(property1,  property2,  property3)
  }

  @Test
  fun `Export & Import Database MetaData`() {
    val hsqlFile = File.createTempFile("hsqldb", ".json").apply { deleteOnExit() }

    DatabaseMetaDataExporterTool(connectorRepository, HSQLDB).export(hsqlFile)

    println(hsqlFile.readText())
  }
}

@Serializable
data class DatabaseProperty(val propertyName: String, val value: ValueType)
