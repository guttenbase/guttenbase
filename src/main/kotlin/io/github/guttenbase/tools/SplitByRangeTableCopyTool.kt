package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.InsertStatementCreator
import io.github.guttenbase.statements.InsertStatementFiller
import io.github.guttenbase.statements.SplitByColumnSelectCountStatementCreator
import io.github.guttenbase.statements.SplitByColumnSelectStatementCreator
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import kotlin.math.min

/**
 * Sometimes the amount of data exceeds any buffer. In these cases we need to split the data by some given range, usually the
 * primary key. I.e., the data is read in chunks where these chunks are split using the ID column range of values. Copy all tables
 * from one connection to the other splitting the input with the given column. If the number range is populated sparsely the
 * copying may take much longer than the [DefaultTableCopyTool].
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
open class SplitByRangeTableCopyTool(connectorRepository: ConnectorRepository) : AbstractTableCopyTool(connectorRepository) {
  /**
   * Copy data with multiple VALUES-tuples per batch statement.
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  override fun copyTable(
    sourceConnectorId: String, sourceConnection: Connection,
    sourceDatabaseConfiguration: SourceDatabaseConfiguration, sourceTableMetaData: TableMetaData,
    sourceTableName: String, targetConnectorId: String, targetConnection: Connection,
    targetDatabaseConfiguration: TargetDatabaseConfiguration, targetTableMetaData: TableMetaData,
    targetTableName: String, numberOfRowsPerBatch: Int, useMultipleValuesClauses: Boolean
  ) {
    val insertStatementCreator = InsertStatementCreator(connectorRepository, targetConnectorId)
    val insertStatementFiller = InsertStatementFiller(connectorRepository)
    val minMaxIdSelector = MinMaxIdSelectorTool(connectorRepository)
    minMaxIdSelector.computeMinMax(sourceConnectorId, sourceTableMetaData, sourceConnection)
    val minValue = minMaxIdSelector.minValue
    val maxValue = minMaxIdSelector.maxValue
    val countStatement: PreparedStatement = SplitByColumnSelectCountStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, sourceTableName, sourceTableMetaData)
    val selectStatement: PreparedStatement = SplitByColumnSelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, sourceTableName, sourceTableMetaData).apply {
        fetchSize = min(numberOfRowsPerBatch, maxRows)
      }

    var totalWritten = 0
    var splitColumnValue = minValue

    while (splitColumnValue <= maxValue) {
      val start = splitColumnValue
      val end = splitColumnValue + numberOfRowsPerBatch
      sourceDatabaseConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
      val countData = getCurrentCount(countStatement, start, end)
      sourceDatabaseConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
      if (countData > 0) {
        progressIndicator.startExecution()
        selectStatement.setLong(1, start)
        selectStatement.setLong(2, end)
        sourceDatabaseConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
        val resultSet = selectStatement.executeQuery()
        sourceDatabaseConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
        targetDatabaseConfiguration.beforeInsert(targetConnection, targetConnectorId, targetTableMetaData)
        val bulkInsert: PreparedStatement = insertStatementCreator.createInsertStatement(
          sourceConnectorId, sourceTableMetaData,
          targetTableName, targetTableMetaData, targetConnection, countData.toInt(), useMultipleValuesClauses
        )
        insertStatementFiller.fillInsertStatementFromResultSet(
          sourceConnectorId, sourceTableMetaData, targetConnectorId,
          targetTableMetaData, targetDatabaseConfiguration, targetConnection, resultSet, bulkInsert, countData.toInt(),
          useMultipleValuesClauses
        )
        bulkInsert.executeBatch()
        if (targetDatabaseConfiguration.isMayCommit) {
          targetConnection.commit()
        }
        insertStatementFiller.clear()
        totalWritten += countData.toInt()
        progressIndicator.endExecution(totalWritten)
        if (resultSet.next()) {
          progressIndicator.warn("Uncopied data!!!")
        }
        resultSet.close()
        bulkInsert.close()
        targetDatabaseConfiguration.afterInsert(targetConnection, targetConnectorId, targetTableMetaData)
      }
      splitColumnValue += (numberOfRowsPerBatch + 1).toLong()
    }
    countStatement.close()
    selectStatement.close()
  }

  @Throws(SQLException::class)
  private fun getCurrentCount(countStatement: PreparedStatement, start: Long, end: Long): Long {
    countStatement.setLong(1, start)
    countStatement.setLong(2, end)
    val countQuery = countStatement.executeQuery()
    countQuery.next()
    return countQuery.getLong(1)
  }
}
