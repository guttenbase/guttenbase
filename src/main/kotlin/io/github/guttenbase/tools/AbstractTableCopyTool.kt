package io.github.guttenbase.tools

import io.github.guttenbase.configuration.SourceDatabaseConfiguration
import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.exceptions.TableConfigurationException
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.TableCopyProgressIndicator
import java.sql.Connection
import java.sql.SQLException

/**
 * Copy all tables from one connection to the other.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [NumberOfRowsPerBatchHint] to determine number of VALUES clauses in INSERT statement
 * Hint is used by [MaxNumberOfDataItemsHint] to determine maximum number of data items in INSERT statement
 * Hint is used by [TableOrderHint] to determine order of tables
 */
abstract class AbstractTableCopyTool(protected val connectorRepository: ConnectorRepository) {
  protected lateinit var progressIndicator: TableCopyProgressIndicator

  /**
   * Copy tables from source to target.
   */
  @Throws(SQLException::class)
  fun copyTables(sourceConnectorId: String, targetConnectorId: String) {
    progressIndicator = connectorRepository.getConnectorHint(targetConnectorId, TableCopyProgressIndicator::class.java).value
    progressIndicator.initializeIndicator()
    val tableSourceMetaDatas = TableOrderHint.getSortedTables(connectorRepository, sourceConnectorId)
    val numberOfRowsPerInsertionHint =
      connectorRepository.getConnectorHint(targetConnectorId, NumberOfRowsPerBatch::class.java).value
    val maxNumberOfDataItemsHint =
      connectorRepository.getConnectorHint(targetConnectorId, MaxNumberOfDataItems::class.java).value
    val sourceDatabaseConfiguration = connectorRepository.getSourceDatabaseConfiguration(sourceConnectorId)
    val targetDatabaseConfiguration = connectorRepository.getTargetDatabaseConfiguration(targetConnectorId)
    val sourceTableMapper = connectorRepository.getConnectorHint(sourceConnectorId, TableMapper::class.java).value
    val targetTableMapper = connectorRepository.getConnectorHint(targetConnectorId, TableMapper::class.java).value
    val refreshTargetConnection =
      connectorRepository.getConnectorHint(targetConnectorId, RefreshTargetConnection::class.java).value
    val sourceDatabaseMetaData = connectorRepository.getDatabaseMetaData(sourceConnectorId)
    val targetDatabaseMetaData = connectorRepository.getDatabaseMetaData(targetConnectorId)
    val sourceConnector = connectorRepository.createConnector(sourceConnectorId)
    val targetConnector = connectorRepository.createConnector(targetConnectorId)
    val sourceConnection = sourceConnector.openConnection()
    var targetConnection = targetConnector.openConnection()

    sourceDatabaseConfiguration.initializeSourceConnection(sourceConnection, sourceConnectorId)
    targetDatabaseConfiguration.initializeTargetConnection(targetConnection, targetConnectorId)
    progressIndicator.startProcess(tableSourceMetaDatas.size)

    var noCopiedTables = 0
    for (sourceTableMetaData in tableSourceMetaDatas) {
      val targetTableMetaData = targetTableMapper.map(sourceTableMetaData, targetDatabaseMetaData)
        ?: throw TableConfigurationException("No matching table for $sourceTableMetaData in target data base!!!")

      val defaultNumberOfRowsPerBatch = numberOfRowsPerInsertionHint.getNumberOfRowsPerBatch(targetTableMetaData)
      val useMultipleValuesClauses = numberOfRowsPerInsertionHint.useMultipleValuesClauses(targetTableMetaData)
      val maxNumberOfDataItems = maxNumberOfDataItemsHint.getMaxNumberOfDataItems(targetTableMetaData)

      val sourceTableName: String = sourceTableMapper.fullyQualifiedTableName(sourceTableMetaData, sourceDatabaseMetaData)
      val targetTableName: String = targetTableMapper.fullyQualifiedTableName(targetTableMetaData, targetDatabaseMetaData)
      val targetRowCount: Int = targetTableMetaData.filteredRowCount
      if (targetRowCount > 0) {
        progressIndicator.warn("Target table " + targetTableMetaData.tableName + " is not empty!")
      }
      var numberOfRowsPerBatch = defaultNumberOfRowsPerBatch
      val columnCount: Int = targetTableMetaData.columnCount
      if (columnCount * numberOfRowsPerBatch > maxNumberOfDataItems) {
        numberOfRowsPerBatch = maxNumberOfDataItems / columnCount
        progressIndicator.debug(
          "Max number of data items " + maxNumberOfDataItems
              + " exceeds numberOfValuesClauses * columns="
              + defaultNumberOfRowsPerBatch
              + " * "
              + columnCount
              + ". Trim number of VALUES clauses to "
              + numberOfRowsPerBatch
        )
      }
      sourceDatabaseConfiguration.beforeTableCopy(sourceConnection, sourceConnectorId, sourceTableMetaData)
      targetDatabaseConfiguration.beforeTableCopy(targetConnection, targetConnectorId, targetTableMetaData)
      progressIndicator.startCopyTable(sourceTableName, sourceTableMetaData.filteredRowCount, targetTableName)
      copyTable(
        sourceConnectorId, sourceConnection, sourceDatabaseConfiguration, sourceTableMetaData, sourceTableName,
        targetConnectorId, targetConnection, targetDatabaseConfiguration, targetTableMetaData, targetTableName,
        numberOfRowsPerBatch, useMultipleValuesClauses
      )
      sourceDatabaseConfiguration.afterTableCopy(sourceConnection, sourceConnectorId, sourceTableMetaData)
      targetDatabaseConfiguration.afterTableCopy(targetConnection, targetConnectorId, targetTableMetaData)
      progressIndicator.endProcess()
      if (refreshTargetConnection.refreshConnection(noCopiedTables++, sourceTableMetaData)) {
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
    connectorRepository.refreshDatabaseMetaData(targetConnectorId)
  }

  @Throws(SQLException::class)
  protected abstract fun copyTable(
    sourceConnectorId: String, sourceConnection: Connection,
    sourceDatabaseConfiguration: SourceDatabaseConfiguration, sourceTableMetaData: TableMetaData,
    sourceTableName: String, targetConnectorId: String, targetConnection: Connection,
    targetDatabaseConfiguration: TargetDatabaseConfiguration, targetTableMetaData: TableMetaData,
    targetTableName: String, numberOfRowsPerBatch: Int, useMultipleValuesClauses: Boolean
  )
}
