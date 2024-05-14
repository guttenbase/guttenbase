package io.github.guttenbase.tools

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint

/**
 * Create auto-increment sequences for table IDs.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Uses [io.github.guttenbase.hints.TableMapperHint]
 * Uses [io.github.guttenbase.hints.EntityTableCheckerHint] to look for entity classes, i.e. classes that may use
 * an ID sequence
 */
@Suppress("MemberVisibilityCanBePrivate", "unused")
abstract class AbstractSequenceCreationTool(protected val connectorRepository: ConnectorRepository) {
  protected val scriptExecutor = ScriptExecutorTool(connectorRepository)

  fun createSequences(connectorId: String, start: Long, incrementBy: Long) {
    val tableMetaDatas = TableOrderHint.getSortedTables(connectorRepository, connectorId)
    val entityTableChecker =
      connectorRepository.hint<EntityTableChecker>(connectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val updateClauses = ArrayList<String>()

    for (tableMetaData in tableMetaDatas) {
      if (entityTableChecker.isEntityTable(tableMetaData)) {
        val tableName = tableMapper.mapTableName(tableMetaData, tableMetaData.databaseMetaData)
        val sequenceName = getSequenceName(tableName)

        updateClauses.addAll(getCreateSequenceClauses(tableName, getIdColumn(tableMetaData), sequenceName, start, incrementBy))
      }
    }

    scriptExecutor.executeScript(connectorId, false, false, updateClauses)
  }

  protected abstract fun getIdColumn(tableMetaData: TableMetaData): String

  protected abstract fun getCreateSequenceClauses(
    tableName: String,
    idColumn: String,
    sequenceName: String,
    start: Long,
    incrementBy: Long
  ): List<String>

  protected abstract fun getSequenceName(tableName: String): String
}
