package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.TableConfigurationException
import io.github.guttenbase.exceptions.UnequalDataException
import io.github.guttenbase.exceptions.UnequalNumberOfRowsException
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.*
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseType.ORACLE
import io.github.guttenbase.meta.DatabaseType.POSTGRESQL
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.statements.SelectStatementCreator
import io.github.guttenbase.utils.Util.LEFT_RIGHT_ARROW
import io.github.guttenbase.utils.Util.RIGHT_ARROW
import io.github.guttenbase.utils.Util.roundToWholeSeconds
import io.github.guttenbase.utils.Util.toDate
import io.github.guttenbase.utils.Util.toLocalDate
import io.github.guttenbase.utils.Util.toLocalDateTime
import org.slf4j.LoggerFactory
import java.math.BigDecimal
import java.sql.Blob
import java.sql.Clob
import java.sql.Connection
import java.sql.JDBCType
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Time
import java.util.Date
import javax.management.openmbean.SimpleType.BIGDECIMAL
import kotlin.math.min

/**
 * Check two schemas for equal data where the tool takes a configurable number of sample data from each table.
 *
 *  &copy; 2012-2034 akquinet tech@spree
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
  private val trimStrings : Boolean = true
) {
  @Throws(SQLException::class)
  fun checkTableData() {
    val tableSourceMetaDatas = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val numberOfCheckData =
      connectorRepository.hint<NumberOfCheckedTableData>(sourceConnectorId).numberOfCheckedTableData
    val tableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
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

    for (tableSourceMetaData in tableSourceMetaDatas) {
      val tableDestMetaData = tableMapper.map(tableSourceMetaData, targetDatabaseMetaData)
        ?: throw TableConfigurationException("No matching table for $tableSourceMetaData in target data base!!!")

      if (tableDestMetaData.primaryKeyColumns.isEmpty()) {
        LOG.warn("No primary key column found for $tableDestMetaData!")

        if (tableDestMetaData.totalRowCount > 200) {
          LOG.warn("Cannot check data on table $tableDestMetaData")
        } else {
          LOG.warn("Checking equality for table $tableDestMetaData by reading full data")

          val data1 = ReadTableDataTool(connectorRepository, sourceConnectorId, tableSourceMetaData).start().use {
            it.readTableData(-1).toSet()
          }
          val data2 = ReadTableDataTool(connectorRepository, targetConnectorId, tableDestMetaData).start().use {
            it.readTableData(-1).toSet()
          }

          if (data1 != data2) {
            LOG.warn("Could not validate equality for $tableDestMetaData")
          }
        }
      } else {
        checkTableData(
          sourceConnectorId, connection1, sourceDatabaseConfiguration1, tableSourceMetaData, targetConnectorId,
          connection2, sourceDatabaseConfiguration2, tableDestMetaData, numberOfCheckData
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
    sourceConfiguration: SourceDatabaseConfiguration, sourceTableMetaData: TableMetaData,
    targetConnectorId: String, targetConnection: Connection, targetConfiguration: SourceDatabaseConfiguration,
    targetTableMetaData: TableMetaData, numberOfCheckData: Int
  ) {
    val tableName1 = connectorRepository.hint<TableMapper>(sourceConnectorId)
      .fullyQualifiedTableName(sourceTableMetaData, sourceTableMetaData.databaseMetaData)
    val tableName2 = connectorRepository.hint<TableMapper>(targetConnectorId)
      .fullyQualifiedTableName(targetTableMetaData, targetTableMetaData.databaseMetaData)
    val sourceColumnNameMapper = connectorRepository.hint<ColumnMapper>(sourceConnectorId)
    val targetColumnNameMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)

    checkRowCount(sourceTableMetaData, targetTableMetaData, tableName1, tableName2)

    val selectStatement1 = SelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, tableName1, sourceTableMetaData)
    selectStatement1.fetchSize = numberOfCheckData
    sourceConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val resultSet1 = selectStatement1.executeQuery()
    sourceConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val selectStatement2 = SelectStatementCreator(connectorRepository, targetConnectorId)
      .createMappedSelectStatement(
        targetConnection, sourceTableMetaData, tableName2, targetTableMetaData
      )
    selectStatement2.fetchSize = numberOfCheckData
    targetConfiguration.beforeSelect(targetConnection, targetConnectorId, targetTableMetaData)
    val resultSet2 = selectStatement2.executeQuery()
    targetConfiguration.afterSelect(targetConnection, targetConnectorId, targetTableMetaData)
    val orderedSourceColumns =
      ColumnOrderHint.getSortedColumns(connectorRepository, sourceTableMetaData)
    val columnMapper = connectorRepository.hint<ColumnMapper>(targetConnectorId)
    var rowIndex = 1
    val primaryKeyColumn = sourceTableMetaData.primaryKeyColumns.firstOrNull()

    try {
      while (resultSet1.next() && resultSet2.next() && rowIndex <= numberOfCheckData) {
        var targetColumnIndex = 1
        var currentID = "<UNKNOWN>"

        for (sourceColumnIndex in 1..orderedSourceColumns.size) {
          val sourceColumn = orderedSourceColumns[sourceColumnIndex - 1]
          val mapping = columnMapper.map(sourceColumn, targetTableMetaData)

          for (targetColumn in mapping.columns) {
            val columnMapping = ColumnDataMappingTool(connectorRepository).getCommonColumnTypeMapping(
              sourceColumn, targetColumn
            ) ?: throw IllegalStateException("Could not find type mapping for $sourceColumn $RIGHT_ARROW $targetColumn")
            val columnName1 = sourceColumnNameMapper.mapColumnName(sourceColumn, targetTableMetaData)
            val columnName2 = targetColumnNameMapper.mapColumnName(targetColumn, targetTableMetaData)

            checkColumnTypeMapping(
              tableName1, columnMapping, columnName1, columnName2
            )

            val (value, _) = checkColumnData(
              currentID,
              sourceConnectorId, targetConnectorId, tableName1,
              resultSet1, resultSet2, rowIndex,
              targetColumnIndex, sourceColumnIndex, columnMapping, columnName1,
              sourceColumn, targetColumn
            )

            if (sourceColumn == primaryKeyColumn) {
              currentID = value.toString()
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
    sourceConnectorId: String, targetConnectorId: String,
    tableName: String, resultSet1: ResultSet, resultSet2: ResultSet,
    rowIndex: Int, targetColumnIndex: Int, sourceColumnIndex: Int,
    mapping: ColumnMapping, columnName: String,
    sourceColumn: ColumnMetaData, targetColumn: ColumnMetaData
  ): Pair<Any?, Any?> {
    val sourceColumnType = mapping.columnDataMapping.sourceColumnType
    val targetColumnType = mapping.columnDataMapping.targetColumnType
    val sourceDatabaseType = connectorRepository.getDatabaseMetaData(sourceConnectorId).databaseType
    val targetDatabaseType = connectorRepository.getDatabaseMetaData(targetConnectorId).databaseType
    var data1 = sourceColumnType.getValue(resultSet1, sourceColumnIndex)
    var data2 = targetColumnType.getValue(resultSet2, targetColumnIndex)

    data1 = mapping.columnDataMapping.columnDataMapper.map(mapping, data1)
    data1 = convertData(sourceColumnType, sourceDatabaseType, data1)
    data2 = convertData(targetColumnType, targetDatabaseType, data2)

    when {
      data1 == null && data2 != null -> throw createUnequalDataException(
        tableName, primaryKey, rowIndex, sourceColumnType, columnName, data1, data2, sourceColumn, targetColumn
      )

      data1 != null && data2 == null -> {
        // Oracle has a weird concept of empty strings
        // https://stackoverflow.com/questions/13278773/null-vs-empty-string-in-oracle
        if (data1 is String && targetDatabaseType != ORACLE) {
          throw createUnequalDataException(
            tableName, primaryKey, rowIndex, sourceColumnType, columnName, data1, data2, sourceColumn, targetColumn
          )
        }
      }

      data1 != null && data2 != null && !equalsValue(data1, data2, mapping.columnTypeDefinition.sourceColumn) ->
        throw createUnequalDataException(
          tableName, primaryKey, rowIndex, sourceColumnType, columnName, data1, data2, sourceColumn, targetColumn
        )
    }

    return data1 to data2
  }

  private fun convertData(
    columnType: ColumnType, databaseType: DatabaseType, data: Any?
  ): Any? = when (columnType) {
    CLASS_STRING -> {
      // See http://www.postgresql.org/docs/8.3/static/datatype-character.html
      if (trimStrings) {
        trim(data as String?)
      } else if (data is Clob) {
        createStringFromClob(data)
      } else {
        data
      }
    }

    CLASS_CLOB -> {
      val clob = data as Clob?
      createStringFromClob(clob)
    }

    CLASS_BLOB -> {
      val blob = data as Blob?
      createStringFromBlob(blob)
    }

    else -> data
  }


  private fun equalsValue(data1: Any, data2: Any, sourceColumn: ColumnMetaData) = when {
    data1 is Date && data2 is Date -> data1.toDate().toLocalDateTime().roundToWholeSeconds() ==
        data2.toDate().toLocalDateTime().roundToWholeSeconds()

    sourceColumn.columnTypeName == "YEAR" -> (data1 as Number).toInt() == (data2 as Number).toInt()

    data1 is ByteArray -> data1.contentEquals(data2 as ByteArray)

    data1 is Array<*> -> data1.contentEquals(data2 as Array<*>)

    sourceColumn.jdbcColumnType == JDBCType.DECIMAL -> {
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
    tableName1: String, mapping: ColumnMapping?, columnName1: String, columnName2: String
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

    @Throws(SQLException::class)
    private fun createStringFromBlob(blob: Blob?): String? =
      if (blob == null) null else String(blob.getBytes(1, min(blob.length(), 1000).toInt()))

    @Throws(SQLException::class)
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
