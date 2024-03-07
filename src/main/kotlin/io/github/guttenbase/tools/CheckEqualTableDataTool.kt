package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType.POSTGRESQL
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.exceptions.IncompatibleColumnsException
import io.github.guttenbase.exceptions.TableConfigurationException
import io.github.guttenbase.exceptions.UnequalDataException
import io.github.guttenbase.exceptions.UnequalNumberOfRowsException
import io.github.guttenbase.hints.ColumnOrderHint
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.mapping.ColumnTypeMapping
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.ColumnType
import io.github.guttenbase.meta.ColumnType.CLASS_BLOB
import io.github.guttenbase.meta.ColumnType.CLASS_STRING
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.SelectStatementCreator
import org.slf4j.LoggerFactory
import java.sql.*
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
open class CheckEqualTableDataTool(private val connectorRepository: ConnectorRepository) {
  @Throws(SQLException::class)
  fun checkTableData(sourceConnectorId: String, targetConnectorId: String) {
    val tableSourceMetaDatas = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val numberOfCheckData =
      connectorRepository.getConnectorHint(sourceConnectorId, NumberOfCheckedTableData::class.java)
        .value.numberOfCheckedTableData
    val tableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val sourceDatabaseConfiguration1: SourceDatabaseConfiguration = connectorRepository
      .getSourceDatabaseConfiguration(sourceConnectorId)
    val sourceDatabaseConfiguration2: SourceDatabaseConfiguration = connectorRepository
      .getSourceDatabaseConfiguration(targetConnectorId)
    val connector1 = connectorRepository.createConnector(sourceConnectorId)
    val connector2 = connectorRepository.createConnector(targetConnectorId)
    val connection1: Connection = connector1.openConnection()
    val connection2: Connection = connector2.openConnection()

    sourceDatabaseConfiguration1.initializeSourceConnection(connection1, sourceConnectorId)
    sourceDatabaseConfiguration2.initializeSourceConnection(connection2, targetConnectorId)

    for (tableSourceMetaData in tableSourceMetaDatas) {
      val tableDestMetaData = tableMapper.map(tableSourceMetaData, targetDatabaseMetaData)
        ?: throw TableConfigurationException("No matching table for $tableSourceMetaData in target data base!!!")

      checkTableData(
        sourceConnectorId, connection1, sourceDatabaseConfiguration1, tableSourceMetaData, targetConnectorId,
        connection2, sourceDatabaseConfiguration2, tableDestMetaData, numberOfCheckData
      )
    }

    sourceDatabaseConfiguration1.finalizeSourceConnection(connection1, sourceConnectorId)
    sourceDatabaseConfiguration2.finalizeSourceConnection(connection2, targetConnectorId)
    connector1.closeConnection()
    connector2.closeConnection()
  }

  private fun checkTableData(
    sourceConnectorId: String, sourceConnection: Connection,
    sourceConfiguration: SourceDatabaseConfiguration, sourceTableMetaData: TableMetaData,
    targetConnectorId: String, targetConnection: Connection, targetConfiguration: SourceDatabaseConfiguration,
    targetTableMetaData: TableMetaData, numberOfCheckData: Int
  ) {
    val tableName1 = connectorRepository.getConnectorHint(sourceConnectorId, TableMapper::class.java).value
      .fullyQualifiedTableName(sourceTableMetaData, sourceTableMetaData.databaseMetaData)
    val tableName2 = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
      .fullyQualifiedTableName(targetTableMetaData, targetTableMetaData.databaseMetaData)
    val commonColumnTypeResolver = CommonColumnTypeResolverTool(connectorRepository)
    val sourceColumnNameMapper = connectorRepository.getConnectorHint(sourceConnectorId, ColumnMapper::class.java).value
    val targetColumnNameMapper = connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value

    checkRowCount(sourceTableMetaData, targetTableMetaData, tableName1, tableName2)

    val selectStatement1 = SelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, tableName1, sourceTableMetaData)
    selectStatement1.fetchSize = numberOfCheckData
    sourceConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val resultSet1 = selectStatement1.executeQuery()
    sourceConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val selectStatement2 = SelectStatementCreator(connectorRepository, targetConnectorId)
      .createMappedSelectStatement(
        targetConnection, sourceTableMetaData, tableName2, targetTableMetaData, sourceConnectorId, targetConnectorId
      )
    selectStatement2.fetchSize = numberOfCheckData
    targetConfiguration.beforeSelect(targetConnection, targetConnectorId, targetTableMetaData)
    val resultSet2 = selectStatement2.executeQuery()
    targetConfiguration.afterSelect(targetConnection, targetConnectorId, targetTableMetaData)
    val orderedSourceColumns: List<ColumnMetaData> = ColumnOrderHint.getSortedColumns(
      connectorRepository, sourceConnectorId, sourceTableMetaData
    )
    val columnMapper: ColumnMapper =
      connectorRepository.getConnectorHint(targetConnectorId, ColumnMapper::class.java).value
    var rowIndex = 1

    try {
      while (resultSet1.next() && resultSet2.next() && rowIndex <= numberOfCheckData) {
        var targetColumnIndex = 1

        for (sourceColumnIndex in 1..orderedSourceColumns.size) {
          val sourceColumn = orderedSourceColumns[sourceColumnIndex - 1]
          val mapping = columnMapper.map(sourceColumn, targetTableMetaData)

          for (targetColumn in mapping.columns) {
            val columnTypeMapping =
              commonColumnTypeResolver.getCommonColumnTypeMapping(sourceColumn, targetConnectorId, targetColumn)
                ?: throw IllegalStateException("Could not find type mapping for $sourceColumn -> $targetColumn")
            val columnName1 = sourceColumnNameMapper.mapColumnName(sourceColumn, targetTableMetaData)
            val columnName2 = targetColumnNameMapper.mapColumnName(targetColumn, targetTableMetaData)
            checkColumnTypeMapping(tableName1, sourceColumn, targetColumn, columnTypeMapping, columnName1, columnName2)

            val sourceColumnType: ColumnType = columnTypeMapping.sourceColumnType

            checkData(
              sourceConnectorId,
              targetConnectorId,
              tableName1,
              resultSet1,
              resultSet2,
              rowIndex,
              targetColumnIndex,
              sourceColumnIndex,
              sourceColumn,
              targetColumn,
              columnTypeMapping,
              columnName1,
              sourceColumnType
            )
          }

          targetColumnIndex += mapping.columns.size
        }

        rowIndex++
      }
    } finally {
      closeEverything(selectStatement1, resultSet1, selectStatement2, resultSet2)
    }

    LOG.info("Checking data of $tableName1 <--> $tableName2 finished")
  }

  private fun checkData(
    sourceConnectorId: String,
    targetConnectorId: String,
    tableName1: String,
    resultSet1: ResultSet,
    resultSet2: ResultSet,
    rowIndex: Int,
    targetColumnIndex: Int,
    sourceColumnIndex: Int,
    sourceColumn: ColumnMetaData,
    columnMetaData2: ColumnMetaData,
    columnTypeMapping: ColumnTypeMapping,
    columnName1: String,
    sourceColumnType: ColumnType
  ) {
    var data1: Any? = sourceColumnType.getValue(resultSet1, sourceColumnIndex)
    data1 = columnTypeMapping.columnDataMapper.map(sourceColumn, columnMetaData2, data1)
    var data2: Any? = columnTypeMapping.targetColumnType.getValue(resultSet2, targetColumnIndex)

    when (sourceColumnType) {
      CLASS_STRING -> {
        val connectionInfo1: ConnectorInfo = connectorRepository.getConnectionInfo(sourceConnectorId)
        val connectionInfo2: ConnectorInfo = connectorRepository.getConnectionInfo(targetConnectorId)

        // See http://www.postgresql.org/docs/8.3/static/datatype-character.html
        if (POSTGRESQL == connectionInfo1.databaseType || POSTGRESQL == connectionInfo2.databaseType) {
          data1 = trim(data1 as String?)
          data2 = trim(data2 as String?)
        }
      }

      CLASS_BLOB -> {
        val blob1: Blob? = data1 as Blob?
        val blob2: Blob? = data2 as Blob?
        data1 = createStringFromBlob(blob1)
        data2 = createStringFromBlob(blob2)
      }

      else -> {}
    }
    if (data1 == null && data2 != null || data1 != null && data2 == null) {
      throw createIncompatibleDataException(tableName1, rowIndex, sourceColumnType, columnName1, data1, data2)
    } else if (data1 != null && data2 != null && data1 != data2) {
      throw createIncompatibleDataException(tableName1, rowIndex, sourceColumnType, columnName1, data1, data2)
    }
  }

  private fun checkRowCount(
    sourceTableMetaData: TableMetaData,
    targetTableMetaData: TableMetaData,
    tableName1: String,
    tableName2: String
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
    LOG.info("Checking data of $tableName1 <--> $tableName2 started")
  }

  private fun checkColumnTypeMapping(
    tableName1: String,
    sourceColumn: ColumnMetaData,
    columnMetaData2: ColumnMetaData,
    columnTypeMapping: ColumnTypeMapping?,
    columnName1: String,
    columnName2: String
  ) {
    if (columnTypeMapping == null) {
      throw IncompatibleColumnsException(
        (tableName1 + ": Columns have incompatible types: "
            + columnName1
            + "/"
            + sourceColumn.columnTypeName
            ) + " vs. "
            + columnName2
            + "/"
            + columnMetaData2.columnTypeName
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

    private fun trim(data: String?): String? = data?.trim { it <= ' ' }

    private fun createIncompatibleDataException(
      tableName: String, index: Int,
      columnType: ColumnType, columnName: String, data1: Any?, data2: Any?
    ) = UnequalDataException(
      tableName + ": Row "
          + index
          + ": Data not equal on column "
          + columnName
          + ": \n'"
          + data1
          + "'\n vs. \n'"
          + data2
          + "'\n, column class = "
          + columnType.columnClasses
    )
  }
}
