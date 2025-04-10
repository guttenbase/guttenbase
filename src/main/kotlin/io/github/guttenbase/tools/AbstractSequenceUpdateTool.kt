package io.github.guttenbase.tools

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Update auto-increment sequences for table IDs.
 *
 * By default the sequence is updated to SELECT(MAX(ID) + 1) FROM table
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 * Uses [io.github.guttenbase.hints.EntityTableCheckerHint] to look for entity classes, i.e. classes that may use an ID sequence
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractSequenceUpdateTool(protected val connectorRepository: ConnectorRepository) {
  protected val scriptExecutor = ScriptExecutorTool(connectorRepository)
  protected val minMaxIdSelector = MinMaxIdSelectorTool(connectorRepository)

  @Suppress("unused")
  fun updateSequences(connectorId: String) {
    val tableMetaDatas = TableOrderHint.getSortedTables(connectorRepository, connectorId)
    val entityTableChecker = connectorRepository.hint<EntityTableChecker>(connectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val updateClauses = ArrayList<String>()

    for (tableMetaData in tableMetaDatas) {
      if (entityTableChecker.isEntityTable(tableMetaData)) {
        minMaxIdSelector.computeMinMax(connectorId, tableMetaData)

        val sequenceValue = minMaxIdSelector.maxValue + 1
        val tableName: String = tableMapper.mapTableName(tableMetaData, tableMetaData.database)
        val sequenceName = getSequenceName(tableName)

        updateClauses.add(getUpdateSequenceClause(sequenceName, sequenceValue))
      }
    }

    scriptExecutor.executeScript(connectorId, false, false, updateClauses)
  }

  abstract fun getUpdateSequenceClause(sequenceName: String, sequenceValue: Long): String

  abstract fun getSequenceName(tableName: String): String
}
