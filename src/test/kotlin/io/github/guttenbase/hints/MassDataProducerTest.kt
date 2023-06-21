package io.github.guttenbase.hints

import io.github.guttenbase.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.hints.impl.DefaultColumnDataMapperProviderHint
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Produce lots of data by duplicating and altering existing entries. IDs have to be adapted of course.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class MassDataProducerTest : AbstractGuttenBaseTest() {
  private val nameDataMapper: ColumnDataMapper = object : ColumnDataMapper {
    override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData) =
      sourceColumnMetaData.columnName.uppercase().endsWith("NAME")

    override fun map(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData, value: Any?) =
      value.toString() + "_" + loopCounter
  }

  private val idDataMapper: ColumnDataMapper = object : ColumnDataMapper {
    override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData) =
      sourceColumnMetaData.columnName.uppercase().endsWith("ID")

    override fun map(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData, value: Any?) =
      value as Long + getOffset(sourceColumnMetaData)
  }

  private val maxTableIds = HashMap<TableMetaData, Long>()
  private var loopCounter = 0

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, resourceName = "/ddl/tables.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(TARGET, resourceName = "/ddl/tables.sql")
    ScriptExecutorTool(connectorRepository).executeFileScript(SOURCE, false, false, "/data/test-data.sql")
    connectorRepository.addConnectorHint(TARGET, object : DefaultColumnDataMapperProviderHint() {
      override fun addMappings(columnDataMapperFactory: DefaultColumnDataMapperProvider) {
        super.addMappings(columnDataMapperFactory)

        columnDataMapperFactory.addMapping(ColumnType.CLASS_STRING, ColumnType.CLASS_STRING, nameDataMapper)
        columnDataMapperFactory.addMapping(ColumnType.CLASS_LONG, ColumnType.CLASS_LONG, idDataMapper)
      }
    })

    computeMaximumIds()
  }

  @Test
  fun testDataDuplicates() {
    loopCounter = 0

    while (loopCounter < MAX_LOOP) {
      DefaultTableCopyTool(connectorRepository).copyTables(SOURCE, TARGET)
      loopCounter++
    }

    val listUserTable = ScriptExecutorTool(connectorRepository).executeQuery(
      TARGET, "SELECT DISTINCT ID, USERNAME, NAME, PASSWORD FROM FOO_USER ORDER BY ID"
    )
    assertEquals(5 * MAX_LOOP, listUserTable.size)
    val listUserCompanyTable: RESULT_LIST = ScriptExecutorTool(connectorRepository).executeQuery(
      TARGET, "SELECT DISTINCT USER_ID, ASSIGNED_COMPANY_ID FROM FOO_USER_COMPANY ORDER BY USER_ID"
    )
    assertEquals(3 * MAX_LOOP, listUserCompanyTable.size)
  }

  private fun getOffset(sourceColumnMetaData: ColumnMetaData): Long {
    val iterator = sourceColumnMetaData.referencedColumns.values.iterator()
    val idColumnMetaData = if (iterator.hasNext()) iterator.next()[0] else sourceColumnMetaData
    val tableMetaData: TableMetaData = idColumnMetaData.tableMetaData
    val maxId = maxTableIds[tableMetaData]
    assertNotNull(maxId, "$sourceColumnMetaData:$tableMetaData")

    return loopCounter * maxId!!
  }

  private fun computeMaximumIds() {
    val tables: List<TableMetaData> = connectorRepository.getDatabaseMetaData(SOURCE).tableMetaData
    val entityTableChecker = connectorRepository.getConnectorHint(SOURCE, EntityTableChecker::class.java)      .value
    val minMaxIdSelectorTool = MinMaxIdSelectorTool(connectorRepository)

    for (tableMetaData in tables) {
      if (entityTableChecker.isEntityTable(tableMetaData)) {
        minMaxIdSelectorTool.computeMinMax(SOURCE, tableMetaData)
        maxTableIds[tableMetaData] = minMaxIdSelectorTool.maxValue
      }
    }
  }

  companion object {
    private const val MAX_LOOP = 5
    const val SOURCE = "SOURCE"
    const val TARGET = "TARGET"
  }
}
