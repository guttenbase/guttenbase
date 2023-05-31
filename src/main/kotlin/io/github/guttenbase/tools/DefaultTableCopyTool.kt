package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.InsertStatementCreator
import io.github.guttenbase.statements.InsertStatementFiller
import io.github.guttenbase.statements.SelectStatementCreator
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * Copy all tables from one connection to the other with multiple VALUES-tuples per batch statement.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultTableCopyTool(connectorRepository: ConnectorRepository) : AbstractTableCopyTool(connectorRepository) {
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
    val sourceRowCount: Int = sourceTableMetaData.filteredRowCount
    val selectStatement: PreparedStatement = SelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, sourceTableName, sourceTableMetaData)

    sourceDatabaseConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val resultSet = selectStatement.executeQuery()
    sourceDatabaseConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)

    val numberOfBatches = sourceRowCount / numberOfRowsPerBatch
    val remainder = sourceRowCount - numberOfBatches * numberOfRowsPerBatch
    val insertStatementCreator = InsertStatementCreator(connectorRepository, targetConnectorId)
    val insertStatementFiller = InsertStatementFiller(connectorRepository)

    targetDatabaseConfiguration.beforeInsert(targetConnection, targetConnectorId, targetTableMetaData)

    val batchInsertStatement: PreparedStatement = insertStatementCreator.createInsertStatement(
      sourceConnectorId,
      sourceTableMetaData, targetTableName, targetTableMetaData, targetConnection, numberOfRowsPerBatch,
      useMultipleValuesClauses
    )

    for (i in 0 until numberOfBatches) {
      progressIndicator.startExecution()

      insertStatementFiller.fillInsertStatementFromResultSet(
        sourceConnectorId, sourceTableMetaData, targetConnectorId,
        targetTableMetaData, targetDatabaseConfiguration, targetConnection, resultSet, batchInsertStatement,
        numberOfRowsPerBatch, useMultipleValuesClauses
      )

      batchInsertStatement.executeBatch()

      if (targetDatabaseConfiguration.isMayCommit) {
        targetConnection.commit()
      }

      insertStatementFiller.clear()
      progressIndicator.endExecution((i + 1) * numberOfRowsPerBatch)
    }

    if (numberOfBatches > 0) {
      batchInsertStatement.close()
    }

    if (remainder > 0) {
      val finalInsert: PreparedStatement = insertStatementCreator.createInsertStatement(
        sourceConnectorId,
        sourceTableMetaData,
        targetTableName,
        targetTableMetaData,
        targetConnection,
        remainder,
        useMultipleValuesClauses
      )

      insertStatementFiller.fillInsertStatementFromResultSet(
        sourceConnectorId, sourceTableMetaData, targetConnectorId,
        targetTableMetaData, targetDatabaseConfiguration, targetConnection, resultSet, finalInsert, remainder,
        useMultipleValuesClauses
      )

      finalInsert.executeBatch()

      if (targetDatabaseConfiguration.isMayCommit) {
        targetConnection.commit()
      }

      insertStatementFiller.clear()
      progressIndicator.endExecution(sourceRowCount)
      finalInsert.close()
    }

    targetDatabaseConfiguration.afterInsert(targetConnection, targetConnectorId, targetTableMetaData)

    if (resultSet.next()) {
      progressIndicator.warn("Uncopied data!!!")
    }

    resultSet.close()
    selectStatement.close()
  }
}
