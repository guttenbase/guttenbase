package io.github.guttenbase.hints

import io.github.guttenbase.io.github.guttenbase.tools.AbstractGuttenBaseTest
import io.github.guttenbase.configuration.TestDerbyConnectionInfo
import io.github.guttenbase.configuration.TestH2ConnectionInfo
import io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider
import io.github.guttenbase.mapping.ColumnDataMapper
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType.CLASS_LONG
import io.github.guttenbase.meta.ColumnType.CLASS_STRING
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.DefaultTableCopyTool
import io.github.guttenbase.tools.EntityTableChecker
import io.github.guttenbase.tools.MinMaxIdSelectorTool
import io.github.guttenbase.tools.RESULT_LIST
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

/**
 * Produce lots of data by duplicating and altering existing entries. IDs have to be adapted of course.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class MassDataProducerTest : AbstractGuttenBaseTest() {
  private val nameDataMapper = object : ColumnDataMapper {
    override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData) =
      sourceColumnMetaData.columnName.uppercase().endsWith("NAME")

    override fun map(mapping: ColumnDataMapping, value: Any?) =
      value.toString() + "_" + loopCounter
  }

  private val idDataMapper = object : ColumnDataMapper {
    override fun isApplicable(sourceColumnMetaData: ColumnMetaData, targetColumnMetaData: ColumnMetaData) =
      sourceColumnMetaData.columnName.uppercase().endsWith("ID")

    override fun map(mapping: ColumnDataMapping, value: Any?) =
      value as Long + getOffset(mapping.sourceColumn)
  }

  private val maxTableIds = HashMap<TableMetaData, Long>()
  private var loopCounter = 0

  @BeforeEach
  fun setup() {
    connectorRepository.addConnectionInfo(SOURCE, TestDerbyConnectionInfo())
    connectorRepository.addConnectionInfo(TARGET, TestH2ConnectionInfo())
    scriptExecutorTool.executeFileScript(SOURCE, resourceName = "/ddl/tables-derby.sql")
    scriptExecutorTool.executeFileScript(TARGET, resourceName = "/ddl/tables-h2.sql")
    scriptExecutorTool.executeFileScript(SOURCE, false, false, "/data/test-data.sql")

    DefaultColumnDataMapperProvider.addMapping(CLASS_STRING, CLASS_STRING, nameDataMapper)
    DefaultColumnDataMapperProvider.addMapping(CLASS_LONG, CLASS_LONG, idDataMapper)

    computeMaximumIds()
  }

  @Test
  fun testDataDuplicates() {
    loopCounter = 0

    while (loopCounter < MAX_LOOP) {
      DefaultTableCopyTool(connectorRepository, SOURCE, TARGET).copyTables()
      loopCounter++
    }

    val listUserTable = scriptExecutorTool.executeQuery(
      TARGET, "SELECT DISTINCT ID, USERNAME, NAME, PASSWORD FROM FOO_USER ORDER BY ID"
    )
    assertEquals(4 * MAX_LOOP, listUserTable.size)
    val listUserCompanyTable: RESULT_LIST = scriptExecutorTool.executeQuery(
      TARGET, "SELECT DISTINCT USER_ID, ASSIGNED_COMPANY_ID FROM FOO_USER_COMPANY ORDER BY USER_ID"
    )
    assertEquals(2 * MAX_LOOP, listUserCompanyTable.size)
  }

  private fun getOffset(sourceColumnMetaData: ColumnMetaData): Long {
    val iterator = sourceColumnMetaData.referencedColumns.values.iterator()
    val idColumnMetaData = if (iterator.hasNext()) iterator.next()[0] else sourceColumnMetaData
    val tableMetaData: TableMetaData = idColumnMetaData.table
    val maxId = maxTableIds[tableMetaData]
    assertNotNull(maxId, "$sourceColumnMetaData:$tableMetaData")

    return loopCounter * maxId!!
  }

  private fun computeMaximumIds() {
    val tables: List<TableMetaData> = connectorRepository.getDatabase(SOURCE).tables
    val entityTableChecker = connectorRepository.hint<EntityTableChecker>(SOURCE)
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
  }
}
