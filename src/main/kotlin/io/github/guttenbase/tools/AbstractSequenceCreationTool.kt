package io.github.guttenbase.tools

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.SQLException

/**
 * Create auto-increment sequences for table IDs.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Uses [de.akquinet.jbosscc.guttenbase.hints.TableMapperHint]
 * Uses [de.akquinet.jbosscc.guttenbase.hints.EntityTableCheckerHint] to look for entity classes, i.e. classes that may use
 * an ID sequence
 */
@Suppress("MemberVisibilityCanBePrivate")
abstract class AbstractSequenceCreationTool(protected val connectorRepository: ConnectorRepository) {
  protected val scriptExecutor = ScriptExecutorTool(connectorRepository)

  @Throws(SQLException::class)
  fun createSequences(connectorId: String, start: Long, incrementBy: Long) {
    val tableMetaDatas = TableOrderHint.getSortedTables(connectorRepository, connectorId)
    val entityTableChecker: EntityTableChecker =
      connectorRepository.getConnectorHint(connectorId, EntityTableChecker::class.java).value
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val updateClauses: MutableList<String> = ArrayList()

    for (tableMetaData in tableMetaDatas) {
      if (entityTableChecker.isEntityTable(tableMetaData)) {
        val tableName: String = tableMapper.mapTableName(tableMetaData, tableMetaData.databaseMetaData)
        val sequenceName = getSequenceName(tableName)

        updateClauses.addAll(getCreateSequenceClauses(tableName, getIdColumn(tableMetaData), sequenceName, start, incrementBy)!!)
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
