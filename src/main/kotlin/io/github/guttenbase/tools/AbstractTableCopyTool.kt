package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.exceptions.TableConfigurationException
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.progress.TableCopyProgressIndicator
import java.sql.Connection

/**
 * Copy all tables from one connection to the other.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.BatchInsertionConfigurationHint] to determine number of VALUES clauses in INSERT statement
 * Hint is used by [TableOrderHint] to determine order of tables
 */
abstract class AbstractTableCopyTool(
  protected val connectorRepository: ConnectorRepository,
  protected val sourceConnectorId: String, protected val targetConnectorId: String
) {
  protected lateinit var progressIndicator: TableCopyProgressIndicator

  /**
   * Copy tables from source to target.
   */
  fun copyTables() {
    progressIndicator = connectorRepository.hint<TableCopyProgressIndicator>(targetConnectorId)
    progressIndicator.initializeIndicator()

    val tableSourceMetaDatas = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val numberOfRowsPerInsertionHint = connectorRepository.hint<BatchInsertionConfiguration>(targetConnectorId)
    val sourceDatabaseConfiguration = connectorRepository.getSourceDatabaseConfiguration(sourceConnectorId)
    val targetDatabaseConfiguration = connectorRepository.getTargetDatabaseConfiguration(targetConnectorId)
    val sourceTableMapper = connectorRepository.hint<TableMapper>(sourceConnectorId)
    val targetTableMapper = connectorRepository.hint<TableMapper>(targetConnectorId)
    val refreshTargetConnection = connectorRepository.hint<RefreshTargetConnection>(targetConnectorId)
    val sourceDatabaseMetaData = connectorRepository.getDatabase(sourceConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabase(targetConnectorId)
    val sourceConnector = connectorRepository.createConnector(sourceConnectorId)
    val targetConnector = connectorRepository.createConnector(targetConnectorId)
    val sourceConnection = sourceConnector.openConnection()
    var targetConnection = targetConnector.openConnection()

    sourceDatabaseConfiguration.initializeSourceConnection(sourceConnection, sourceConnectorId)
    targetDatabaseConfiguration.initializeTargetConnection(targetConnection, targetConnectorId)
    progressIndicator.startProcess(tableSourceMetaDatas.size)

    for ((numberOfCopiedTables, sourceTableMetaData) in tableSourceMetaDatas.withIndex()) {
      val targetTableMetaData = targetTableMapper.map(sourceTableMetaData, targetDatabaseMetaData)
        ?: throw TableConfigurationException("No matching table for $sourceTableMetaData in target data base!!!")
      val defaultNumberOfRowsPerBatch = numberOfRowsPerInsertionHint.getNumberOfRowsPerBatch(targetTableMetaData)
      val useMultipleValuesClauses = numberOfRowsPerInsertionHint.useMultipleValuesClauses(targetTableMetaData)
      val maxNumberOfDataItems = numberOfRowsPerInsertionHint.getMaxNumberOfDataItems(targetTableMetaData)
      val sourceTableName = sourceTableMapper.fullyQualifiedTableName(sourceTableMetaData, sourceDatabaseMetaData)
      val targetTableName = targetTableMapper.fullyQualifiedTableName(targetTableMetaData, targetDatabaseMetaData)
      val targetRowCount = targetTableMetaData.filteredRowCount

      if (targetRowCount > 0) {
        progressIndicator.warn("Target table " + targetTableMetaData.tableName + " is not empty!")
      }

      var numberOfRowsPerBatch = defaultNumberOfRowsPerBatch
      val columnCount: Int = targetTableMetaData.columnCount
      if (columnCount * numberOfRowsPerBatch > maxNumberOfDataItems) {
        numberOfRowsPerBatch = maxNumberOfDataItems / columnCount
        progressIndicator.debug(
          "Max number of data items " + maxNumberOfDataItems
              + " exceeds numberOfValuesClauses * columns=" + defaultNumberOfRowsPerBatch
              + " * " + columnCount
              + ". Trim number of VALUES clauses to " + numberOfRowsPerBatch
        )
      }

      sourceDatabaseConfiguration.beforeTableCopy(sourceConnection, sourceConnectorId, sourceTableMetaData)
      targetDatabaseConfiguration.beforeTableCopy(targetConnection, targetConnectorId, targetTableMetaData)

      progressIndicator.startCopyTable(sourceTableName, sourceTableMetaData.filteredRowCount, targetTableName)

      copyTable(
        sourceConnection, sourceDatabaseConfiguration, sourceTableMetaData, sourceTableName,
        targetConnection, targetDatabaseConfiguration, targetTableMetaData, targetTableName,
        numberOfRowsPerBatch, useMultipleValuesClauses
      )

      sourceDatabaseConfiguration.afterTableCopy(sourceConnection, sourceConnectorId, sourceTableMetaData)
      targetDatabaseConfiguration.afterTableCopy(targetConnection, targetConnectorId, targetTableMetaData)

      progressIndicator.endProcess()

      if (refreshTargetConnection.refreshConnection(numberOfCopiedTables, sourceTableMetaData)) {
        progressIndicator.info("Refreshing target connection.")
        targetDatabaseConfiguration.finalizeTargetConnection(targetConnection, targetConnectorId)
        targetConnector.closeConnection()
        targetConnection = targetConnector.openConnection()
        targetDatabaseConfiguration.initializeTargetConnection(targetConnection, targetConnectorId)
      }
    }

    sourceDatabaseConfiguration.finalizeSourceConnection(sourceConnection, sourceConnectorId)
    targetDatabaseConfiguration.finalizeTargetConnection(targetConnection, targetConnectorId)
    sourceConnector.closeConnection()
    targetConnector.closeConnection()
    progressIndicator.finalizeIndicator()
    connectorRepository.refreshDatabase(targetConnectorId)
  }

  protected abstract fun copyTable(
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
  )
}
