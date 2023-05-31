package io.github.guttenbase.tools

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.SQLException

/**
 * Update auto-increment sequences for table IDs.
 *
 *
 * By default the sequence is updated to SELECT(MAX(ID) + 1) FROM table
 *
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Uses [EntityTableCheckerHint] to look for entity classes, i.e. classes that may use an ID sequence
 */
abstract class AbstractSequenceUpdateTool(protected val connectorRepository: ConnectorRepository) {
  protected val scriptExecutor = ScriptExecutorTool(connectorRepository)
  protected val minMaxIdSelector = MinMaxIdSelectorTool(connectorRepository)

  @Throws(SQLException::class)
  fun updateSequences(connectorId: String) {
    val tableMetaDatas: List<TableMetaData> = TableOrderHint.getSortedTables(connectorRepository, connectorId)
    val entityTableChecker=connectorRepository.getConnectorHint(connectorId, EntityTableChecker::class.java).value
    val tableMapper= connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val updateClauses: MutableList<String> = ArrayList()

    for (tableMetaData in tableMetaDatas) {
      if (entityTableChecker.isEntityTable(tableMetaData)) {
        minMaxIdSelector.computeMinMax(connectorId, tableMetaData)

        val sequenceValue = minMaxIdSelector.maxValue + 1
        val tableName: String = tableMapper.mapTableName(tableMetaData, tableMetaData.databaseMetaData)
        val sequenceName = getSequenceName(tableName)
        updateClauses.add(getUpdateSequenceClause(sequenceName, sequenceValue))
      }
    }

    scriptExecutor.executeScript(connectorId, false, false, updateClauses)
  }

  abstract fun getUpdateSequenceClause(sequenceName: String, sequenceValue: Long): String

  abstract fun getSequenceName(tableName: String): String
}
