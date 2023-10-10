package io.github.guttenbase.tools

import io.github.guttenbase.configuration.MockConnectionInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.defaults.impl.DefaultColumnMapper
import io.github.guttenbase.defaults.impl.DefaultTableMapper
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.IncompatibleTablesException
import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.hints.ColumnMapperHint
import io.github.guttenbase.hints.TableMapperHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.meta.impl.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.impl.ConnectorRepositoryImpl
import io.github.guttenbase.schema.SchemaScriptCreatorTool
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.math.BigInteger
import java.sql.Types

class SchemaScriptCreatorToolTest {
  private lateinit var databaseMetaData: DatabaseMetaData
  private val connectorRepository = object : ConnectorRepositoryImpl() {
    override fun getDatabaseMetaData(connectorId: String): DatabaseMetaData {
      return databaseMetaData
    }
  }.apply { updateRepository(this) }
  private val objectUnderTest = SchemaScriptCreatorTool(connectorRepository, SOURCE, TARGET)

  init {
    databaseMetaData = createDatabaseMetaData(connectorRepository)
  }

  @Test
  fun testMetaData() {
    assertEquals("GuttenBaseDB", databaseMetaData.databaseMetaData.databaseProductName)
    assertEquals(42, databaseMetaData.databaseMetaData.maxColumnNameLength)
  }

  @Test
  fun testTableNameLength() {
    val table: TableMetaData = createTable(
      3, databaseMetaData,
      "SomeTableNameThatIsMuchLongerThanFortyTwoCharactersSupportedByDB"
    )

    assertThatThrownBy { objectUnderTest.createTable(table) }
      .isExactlyInstanceOf(IncompatibleTablesException::class.java)
  }

  @Test
  fun testColumnNameLength() {
    val table = createTable(3, databaseMetaData)
    val columnMetaData = createColumn(table, "SomeColumnNameThatIsMuchLongerThanFortyTwoCharactersSupportedByDB")
    assertThatThrownBy { objectUnderTest.createColumn(columnMetaData) }
      .isExactlyInstanceOf(IncompatibleColumnsException::class.java)
  }

  @Test
  fun testDDL() {
    val tableStatements = objectUnderTest.createTableStatements()
    assertEquals(2, tableStatements.size)
    val createStatement = tableStatements[0]

    assertThat(createStatement).startsWith("CREATE TABLE schemaName.MY_TABLE")
    assertThat(createStatement).contains("ID BIGINT IDENTITY")
    assertThat(createStatement).contains("NAME VARCHAR(100) NOT NULL")

    val indexStatements = objectUnderTest.createIndexStatements()
    assertEquals(2, indexStatements.size)
    val indexStatement = indexStatements[0]

    assertThat(indexStatement).startsWith("CREATE UNIQUE INDEX IDX_NAME_IDX2_MY_TABLE2_1 ON schemaName.MY_TABLE")
    assertThat(indexStatement).contains("NAME")

    val foreignKeyStatements = objectUnderTest.createForeignKeyStatements()
    assertEquals(1, foreignKeyStatements.size)
    val foreignKeyStatement = foreignKeyStatements[0].uppercase()

    assertThat(foreignKeyStatement).startsWith("ALTER TABLE SCHEMANAME.MY_TABLE1 ADD CONSTRAINT FK_")
    assertThat(foreignKeyStatement).endsWith("REFERENCES SCHEMANAME.MY_TABLE2(NAME);")
  }

  @Test
  fun testSchemaColumnTypeMapper() {
    val tableStatements = objectUnderTest.createTableStatements()
    assertEquals(2, tableStatements.size)
    val createStatement = tableStatements[0]

    assertThat(createStatement).startsWith("CREATE TABLE schemaName.MY_TABLE")
    assertThat(createStatement).contains("ID BIGINT IDENTITY(1, 1) PRIMARY KEY")
    assertThat(createStatement).contains("NAME VARCHAR(100) NOT NULL")
  }

  @Test
  fun testCreateConstraintName() {
    assertEquals("FK_NAME_1", objectUnderTest.createConstraintName("FK_", "NAME_", 1))

    val constraintName =
      objectUnderTest.createConstraintName(
        "FK_",
        "AUFTRAG_STELLUNGNAHME_HALTUNGSTELLUNGNAHME_ZU_HALTUNG_ID_PARENT_ID__ID_",
        1
      )
    assertNotEquals("FK_AUFTRAG_STELLUNGNAHME_HALTUNGSTELLUNGNAHME_ZU_HALTUNG_ID_PARENT_ID__ID_1", constraintName)
    assertEquals(42, constraintName.length)
    assertEquals(42, objectUnderTest.targetMaxNameLength)
  }

  @Test
  fun testForeignKey() {
    val foreignKeyMetaData = databaseMetaData.tableMetaData[0].importedForeignKeys[0]
    val sql = objectUnderTest.createForeignKey(foreignKeyMetaData)

    assertEquals(
      "ALTER TABLE schemaName.MY_TABLE1 ADD CONSTRAINT FK_Name FOREIGN KEY (NAME) REFERENCES schemaName.MY_TABLE2(NAME);",
      sql
    )
  }

  @Test
  fun createColumn() {
    val sql = objectUnderTest.addTableColumn(databaseMetaData.tableMetaData[0].columnMetaData[1])
    assertEquals("ALTER TABLE schemaName.MY_TABLE1 ADD NAME VARCHAR(100) NOT NULL;", sql)
  }

  @Test
  fun testIndex() {
    val columnMetaData = databaseMetaData.tableMetaData[0].getColumnMetaData("name")!!
    val index = databaseMetaData.tableMetaData[0].getIndexesContainingColumn(columnMetaData)[0]
    val sql = objectUnderTest.createIndex(index)

    assertEquals("CREATE UNIQUE INDEX Name_IDX1 ON schemaName.MY_TABLE1(NAME);", sql)
  }

  companion object {
    const val SOURCE = "source"
    const val TARGET = "target"
    const val TABLE = "My_Table"
    const val SCHEMA_NAME = "schemaName"

    @JvmStatic
    fun createColumn(tableMetaData: TableMetaDataImpl, columnName: String = "Name"): ColumnMetaData =
      ColumnMetaDataImpl(
        tableMetaData,
        Types.VARCHAR, columnName, "VARCHAR(100)", String::class.java.name,
        false, false, 0, 0
      )

    @JvmStatic
    fun createTable(
      index: Int,
      databaseMetaData: DatabaseMetaData,
      tableName: String = "$TABLE$index"
    ): TableMetaDataImpl {
      val tableMetaData = TableMetaDataImpl(databaseMetaData, tableName, "TABLE", "", "")
      val primaryKeyColumn = ColumnMetaDataImpl(
        tableMetaData,
        Types.BIGINT, "Id", "BIGINT", BigInteger::class.java.name,
        false, true, 0, 0
      ).apply { isPrimaryKey = true }
      val nameColumn = createColumn(tableMetaData)
      val nameColumnIndex = IndexMetaDataImpl(tableMetaData, "Name_IDX$index", true, true, false)

      nameColumnIndex.addColumn(nameColumn)

      tableMetaData.addColumn(primaryKeyColumn)
      tableMetaData.addColumn(nameColumn)
      tableMetaData.addIndex(nameColumnIndex)

      return tableMetaData
    }

    @JvmStatic
    fun createDatabaseMetaData(connectorRepository: ConnectorRepository): DatabaseMetaDataImpl {
      val databaseMetaData = DatabaseMetaDataImpl(
        connectorRepository,
        SOURCE,
        SCHEMA_NAME, mapOf(
          "getMaxColumnNameLength" to 42,
          "getDatabaseProductName" to "GuttenBaseDB"
        ), DatabaseType.GENERIC
      )
      val table1 = createTable(1, databaseMetaData)
      val table2 = createTable(2, databaseMetaData)
      val foreignKeyMeta1 = ForeignKeyMetaDataImpl(
        table1, "FK_Name",
        table1.getColumnMetaData("Name")!!, table2.getColumnMetaData("Name")!!
      )
      val foreignKeyMeta2 = ForeignKeyMetaDataImpl(
        table1, "FK_Name",
        table1.getColumnMetaData("Name")!!, table2.getColumnMetaData("Name")!!
      )

      table1.addImportedForeignKey(foreignKeyMeta1)
      table2.addExportedForeignKey(foreignKeyMeta2)

      databaseMetaData.addTable(table1)
      databaseMetaData.addTable(table2)

      return databaseMetaData
    }

    @JvmStatic
    fun updateRepository(repository: ConnectorRepository) {
      repository.addConnectionInfo(SOURCE, MockConnectionInfo())
      repository.addConnectionInfo(TARGET, MockConnectionInfo())
      repository.addConnectorHint(TARGET, object : TableMapperHint() {
        override val value: TableMapper
          get() = DefaultTableMapper(CaseConversionMode.UPPER)
      })
      repository.addConnectorHint(TARGET, object : ColumnMapperHint() {
        override val value: ColumnMapper
          get() = DefaultColumnMapper(CaseConversionMode.UPPER, "")
      })
    }
  }
}