package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.statements.InsertStatementCreator
import io.github.guttenbase.statements.InsertStatementFiller
import io.github.guttenbase.statements.SelectStatementCreator
import java.sql.Connection
import java.sql.SQLException

/**
 * Copy all tables from one connection to the other with multiple VALUES-tuples per batch statement.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultTableCopyTool(
  connectorRepository: ConnectorRepository, sourceConnectorId: String, targetConnectorId: String
) :
  AbstractTableCopyTool(connectorRepository, sourceConnectorId, targetConnectorId) {
  /**
   * Copy data with multiple VALUES-tuples per batch statement.
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  override fun copyTable(
    sourceConnection: Connection,
    sourceDatabaseConfiguration: SourceDatabaseConfiguration,
    sourceTableMetaData: TableMetaData,
    sourceTableName: String,
    targetConnection: Connection,
    targetDatabaseConfiguration: TargetDatabaseConfiguration,
    targetTableMetaData: TableMetaData,
    targetTableName: String,
    numberOfRowsPerBatch: Int,
    useMultipleValuesClauses: Boolean
  ) {
    val sourceRowCount = sourceTableMetaData.filteredRowCount
    val selectStatement = SelectStatementCreator(connectorRepository, sourceConnectorId)
      .createSelectStatement(sourceConnection, sourceTableName, sourceTableMetaData)

    sourceDatabaseConfiguration.beforeSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)
    val resultSet = selectStatement.executeQuery()
    sourceDatabaseConfiguration.afterSelect(sourceConnection, sourceConnectorId, sourceTableMetaData)

    val numberOfBatches = sourceRowCount / numberOfRowsPerBatch
    val remainder = sourceRowCount - numberOfBatches * numberOfRowsPerBatch
    val insertStatementCreator = InsertStatementCreator(connectorRepository, targetConnectorId)
    val insertStatementFiller = InsertStatementFiller(connectorRepository, targetConnectorId)

    targetDatabaseConfiguration.beforeInsert(targetConnection, targetConnectorId, targetTableMetaData)

    if (numberOfBatches > 0) {
      val batchInsertStatement = insertStatementCreator.createInsertStatement(
        sourceTableMetaData, targetTableName,
        targetTableMetaData, targetConnection, numberOfRowsPerBatch,
        useMultipleValuesClauses
      )

      for (i in 0 until numberOfBatches) {
        progressIndicator.startExecution("Committing")

        insertStatementFiller.fillInsertStatementFromResultSet(
          sourceTableMetaData, targetTableMetaData, targetDatabaseConfiguration,
          targetConnection, resultSet, batchInsertStatement, numberOfRowsPerBatch, useMultipleValuesClauses
        )

        batchInsertStatement.executeBatch()

        if (targetDatabaseConfiguration.isMayCommit) {
          targetConnection.commit()
        }

        insertStatementFiller.clear()
        progressIndicator.endExecution((i + 1) * numberOfRowsPerBatch)
      }

      batchInsertStatement.close()
    }

    if (remainder > 0) {
      val finalInsert = insertStatementCreator.createInsertStatement(
        sourceTableMetaData, targetTableName,
        targetTableMetaData, targetConnection, remainder,
        useMultipleValuesClauses
      )

      insertStatementFiller.fillInsertStatementFromResultSet(
        sourceTableMetaData, targetTableMetaData, targetDatabaseConfiguration,
        targetConnection, resultSet, finalInsert,
        remainder, useMultipleValuesClauses
      )

      if (remainder > 1) {
        finalInsert.executeBatch()
      } else {
        finalInsert.executeUpdate()
      }

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
