package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.TableConfigurationException
import io.github.guttenbase.exceptions.UnequalDataException
import io.github.guttenbase.exceptions.UnequalNumberOfRowsException
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.ColumnDataMapping
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.DatabaseType.ORACLE
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.statements.SelectStatementCreator
import io.github.guttenbase.utils.Util.LEFT_RIGHT_ARROW
import io.github.guttenbase.utils.Util.RIGHT_ARROW
import io.github.guttenbase.utils.Util.roundToWholeSeconds
import io.github.guttenbase.utils.Util.toDate
import io.github.guttenbase.utils.Util.toLocalDateTime
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.*
import java.util.Date
import kotlin.math.min

/**
 * Check two schemas for equal data where the tool takes a configurable number of sample data from each table.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.NumberOfCheckedTableDataHint] How many rows of tables shall be regarded when checking that data has been
 * transferred correctly.
 * Hint is used by [ColumnOrderHint] to determine column order
 * Hint is used by [TableOrderHint] to determine order of tables
 */
open class CheckEqualTableDataTool(
  private val connectorRepository: ConnectorRepository,
  private val sourceConnectorId: String, private val targetConnectorId: String,
  private val maxRowCountCheck: Int = 300
) {
  fun checkTableData() {
    val sourceTables = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val numberOfCheckData =
      connectorRepository.hint<NumberOfCheckedTableData>(sourceConnectorId).numberOfCheckedTableData
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val targetDatabase = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val sourceDatabaseConfiguration1 = connectorRepository
      .getSourceDatabaseConfiguration(sourceConnectorId)
    val sourceDatabaseConfiguration2 = connectorRepository
      .getSourceDatabaseConfiguration(targetConnectorId)
    val sourceConnector = connectorRepository.createConnector(sourceConnectorId)
    val targetConnector = connectorRepository.createConnector(targetConnectorId)
    val connection1 = sourceConnector.openConnection()
    val connection2 = targetConnector.openConnection()

    sourceDatabaseConfiguration1.initializeSourceConnection(connection1, sourceConnectorId)
    sourceDatabaseConfiguration2.initializeSourceConnection(connection2, targetConnectorId)

    for (sourceTable in sourceTables) {
      val targetTable = tableMapper.map(sourceTable, targetDatabase)
        ?: throw TableConfigurationException("No matching table for $sourceTable in target data base!!!")

      if (targetTable.primaryKeyColumns.size != 1) {
        LOG.warn("No/too many primary key column found for $targetTable!")

        if (targetTable.totalRowCount > maxRowCountCheck) {
          LOG.warn("Cannot check data on table $targetTable")
        } else {
          LOG.warn("Checking equality for table $targetTable by reading full data")

          val data1 = ReadTableDataTool(connectorRepository, sourceConnectorId, sourceTable)
            .start(connection1).readTableData(-1).toSet()
          val data2 = ReadTableDataTool(connectorRepository, targetConnectorId, targetTable).start(connection2)
            .readTableData(-1).toSet()

          if (data1 != data2) {
            LOG.warn("Could not validate equality for $targetTable")
          }
        }
      } else {
        checkTableData(
          sourceConnectorId, connection1, sourceDatabaseConfiguration1, sourceTable, targetConnectorId,
          connection2, sourceDatabaseConfiguration2, targetTable, numberOfCheckData
        )
      }
    }

    sourceDatabaseConfiguration1.finalizeSourceConnection(connection1, sourceConnectorId)
    sourceDatabaseConfiguration2.finalizeSourceConnection(connection2, targetConnectorId)
    sourceConnector.closeConnection()
    targetConnector.closeConnection()
  }

  private fun checkTableData(
    sourceConnectorId: String, sourceConnection: Connection,
    sourceConfiguration: SourceDatabaseConfiguration, sourceTable: TableMetaData,
    targetConnectorId: String, targetConnection: Connection, targetConfiguration: SourceDatabaseConfiguration,
    targetTable: TableMetaData, numberOfCheckData: Int
  ) {
    val tableName1 = connectorRepository.hint<TableMapper>(sourceConnectorId)
      .fullyQualifiedTableName(sourceTable, sourceTable.databaseMetaData)
    val tableName2 = connectorRepository.hint<TableMapper>(targetConnectorId)
      .fullyQualifiedTableName(targetTable, targetTable.databaseMetaData)
    val sourceColumnNameMapper = connectorRepository.hint<ColumnMapper>(sourceConnectorId)
    val targetColumnNameMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)

    checkRowCount(sourceTable, targetTable, tableName1, tableName2)

    val selectStatement1 = SelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, tableName1, sourceTable)
    selectStatement1.fetchSize = numberOfCheckData
    sourceConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTable)
    val resultSet1 = selectStatement1.executeQuery()
    sourceConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTable)
    val selectStatement2 = SelectStatementCreator(connectorRepository, targetConnectorId).createMappedSelectStatement(
      targetConnection, sourceTable, tableName2, targetTable
    )
    selectStatement2.fetchSize = numberOfCheckData
    targetConfiguration.beforeSelect(targetConnection, targetConnectorId, targetTable)
    val resultSet2 = selectStatement2.executeQuery()
    targetConfiguration.afterSelect(targetConnection, targetConnectorId, targetTable)
    val orderedSourceColumns = ColumnOrderHint.getSortedColumns(connectorRepository, sourceTable)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    var rowIndex = 1

    try {
      while (resultSet1.next() && resultSet2.next() && rowIndex <= numberOfCheckData) {
        var targetColumnIndex = 1
        val currentID = StringBuilder()

        for (sourceColumnIndex in 1..orderedSourceColumns.size) {
          val sourceColumn = orderedSourceColumns[sourceColumnIndex - 1]
          val mapping = columnMapper.map(sourceColumn, targetTable)

          for (targetColumn in mapping.columns) {
            val columnMapping = ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(
              sourceColumn, targetColumn
            ) ?: throw IllegalStateException("Could not find type mapping for $sourceColumn $RIGHT_ARROW $targetColumn")
            val columnName1 = sourceColumnNameMapper.mapColumnName(sourceColumn, targetTable)
            val columnName2 = targetColumnNameMapper.mapColumnName(targetColumn, targetTable)

            checkColumnTypeMapping(tableName1, columnMapping, columnName1, columnName2)

            val (value, _) = checkColumnData(
              currentID.toString(),
              targetConnectorId, tableName1, resultSet1,
              resultSet2, rowIndex, targetColumnIndex,
              sourceColumnIndex, columnMapping, columnName1
            )

            if (sourceColumn.isPrimaryKey) {
              currentID.append(":").append(value)
            }
          }

          targetColumnIndex += mapping.columns.size
        }

        rowIndex++
      }
    } finally {
      closeEverything(selectStatement1, resultSet1, selectStatement2, resultSet2)
    }

    LOG.info("Checking data of $tableName1 $LEFT_RIGHT_ARROW $tableName2 finished")
  }

  private fun checkColumnData(
    primaryKey: String,
    targetConnectorId: String, tableName: String,
    resultSet1: ResultSet, resultSet2: ResultSet, rowIndex: Int,
    targetColumnIndex: Int, sourceColumnIndex: Int, mapping: ColumnDataMapping,
    columnName: String
  ): Pair<Any?, Any?> {
    val sourceColumnType = mapping.sourceColumnType
    val targetColumnType = mapping.targetColumnType
    val targetDatabaseType = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseType
    var data1 = sourceColumnType.getValue(resultSet1, sourceColumnIndex, mapping.sourceColumn)
    var data2 = targetColumnType.getValue(resultSet2, targetColumnIndex, mapping.targetColumn)

    if (data1?.javaClass != data2?.javaClass) {
      data1 = mapping.columnDataMapper.map(mapping, data1)
      data1 = convertData(data1)
      data2 = convertData(data2)
    }

    when {
      data1 == null && data2 != null -> throw createUnequalDataException(
        tableName, primaryKey, rowIndex, sourceColumnType, columnName, data1, data2, mapping.sourceColumn, mapping.targetColumn
      )

      data1 != null && data2 == null -> {
        // Oracle has a weird concept of empty strings
        // https://stackoverflow.com/questions/13278773/null-vs-empty-string-in-oracle
        if (data1 is String && targetDatabaseType != ORACLE) {
          throw createUnequalDataException(
            tableName, primaryKey, rowIndex, sourceColumnType, columnName,
            data1, data2, mapping.sourceColumn, mapping.targetColumn
          )
        }
      }

      sourceColumnType == ColumnType.CLASS_UNKNOWN || sourceColumnType == ColumnType.CLASS_ARRAY ->
        LOG.warn("Cannot check data for columns ${mapping.sourceColumn}/$sourceColumnType ${mapping.targetColumn}/$targetColumnType")

      data1 != null && data2 != null && !equalsValue(data1, data2, mapping.columnTypeDefinition.sourceColumn) ->
        throw createUnequalDataException(
          tableName, primaryKey, rowIndex, sourceColumnType, columnName,
          data1, data2, mapping.sourceColumn, mapping.targetColumn
        )
    }

    return data1 to data2
  }

  private fun convertData(data: Any?): Any? = when (data) {
    is String? -> {
      // See http://www.postgresql.org/docs/8.3/static/datatype-character.html
      trim(data)
    }

    is Clob -> {
      createStringFromClob(data)
    }

    is ByteArray -> {
      String(data)
    }

    is Blob -> {
      createStringFromBlob(data)
    }

    else -> data
  }


  private fun equalsValue(data1: Any, data2: Any, sourceColumn: ColumnMetaData) = when {
    data1 is Date && data2 is Date -> data1.toDate().toLocalDateTime().roundToWholeSeconds() ==
        data2.toDate().toLocalDateTime().roundToWholeSeconds()

    sourceColumn.columnTypeName == "YEAR" -> (data1 as Number).toInt() == (data2 as Number).toInt()

    data1 is ByteArray -> data1.contentEquals(data2 as ByteArray)

    data1 is Array<*> -> data1.contentEquals(data2 as Array<*>)

    sourceColumn.jdbcColumnType == JDBCType.DECIMAL || sourceColumn.jdbcColumnType == JDBCType.NUMERIC -> {
      if (data1 is BigDecimal && data2 is BigDecimal)
        data1.compareTo(data2) == 0 // Ignore scale, if 0
      else// if (data1 is Long && data2 is Long)
        data1 == data2
    }

    else -> data1 == data2
  }

  private fun checkRowCount(
    sourceTableMetaData: TableMetaData, targetTableMetaData: TableMetaData,
    tableName1: String, tableName2: String
  ) {
    if (sourceTableMetaData.filteredRowCount != targetTableMetaData.filteredRowCount) {
      throw UnequalNumberOfRowsException(
        ("Number of rows is not equal: " + tableName1
            + "="
            + sourceTableMetaData.filteredRowCount
            ) + " vs. "
            + tableName2
            + "="
            + targetTableMetaData.filteredRowCount
      )
    }
    LOG.info("Checking data of $tableName1 $LEFT_RIGHT_ARROW $tableName2 started")
  }

  private fun checkColumnTypeMapping(
    tableName1: String, mapping: ColumnDataMapping?, columnName1: String, columnName2: String
  ) {
    if (mapping == null) {
      throw IncompatibleColumnsException(
        "$tableName1: Columns have incompatible types: $columnName1 vs. $columnName2"
      )
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(CheckEqualTableDataTool::class.java)

    private fun closeEverything(vararg closeables: AutoCloseable) {
      closeables.forEach { it.use { } }
    }

    private fun createStringFromBlob(blob: Blob?): String? =
      if (blob == null) null else String(blob.getBytes(1, min(blob.length(), 1000).toInt()))

    private fun createStringFromClob(clob: Clob?): String? = clob?.characterStream?.readText()

    private fun trim(data: String?): String? = data?.trim { it <= ' ' }

    private fun createUnequalDataException(
      tableName: String, primaryKey: String,
      index: Int, columnType: ColumnType, columnName: String,
      data1: Any?, data2: Any?,
      sourceColumn: ColumnMetaData, targetColumn: ColumnMetaData
    ) = UnequalDataException(
      """|
        |Table $tableName: Row $index, PK '$primaryKey' : Data not equal on column $columnName: 
        |'$data1' (${data1?.javaClass}) ${sourceColumn.columnTypeName}/${sourceColumn.jdbcColumnType}
        |vs. 
        |'$data2' (${data2?.javaClass}) ${targetColumn.columnTypeName}/${targetColumn.jdbcColumnType}
        |column class = ${columnType.columnClasses}""".trimMargin()
    )
  }
}
