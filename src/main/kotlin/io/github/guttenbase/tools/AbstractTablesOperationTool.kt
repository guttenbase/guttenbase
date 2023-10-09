package io.github.guttenbase.tools

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.SQLException

/**
 * Will execute given SQL scriptlet on all tables or single table of given connector. The table name can be referenced with @TABLE@
 * placeholder.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.TableMapperHint]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate")
abstract class AbstractTablesOperationTool(
  protected val connectorRepository: ConnectorRepository,
  protected val template: String
) {
  protected val scriptExecutor = ScriptExecutorTool(connectorRepository)

  @Throws(SQLException::class)
  @JvmOverloads
  fun executeOnAllTables(connectorId: String, updateSchema: Boolean = true, prepareTargetConnection: Boolean = true) {
    val tables = TableOrderHint.getSortedTables(connectorRepository, connectorId)
    val statements = tables.filter { isApplicableOnTable(it) }.map { createSql(connectorId, it) }

    scriptExecutor.executeScript(connectorId, updateSchema, prepareTargetConnection, statements)
  }

  @Throws(SQLException::class)
  @JvmOverloads
  fun executeOnTable(
    connectorId: String,
    tableMetaData: TableMetaData,
    updateSchema: Boolean = true,
    prepareTargetConnection: Boolean = true
  ) {
    if (isApplicableOnTable(tableMetaData)) {
      val sql = createSql(connectorId, tableMetaData)

      scriptExecutor.executeScript(connectorId, updateSchema, prepareTargetConnection, sql)
    }
  }

  private fun createSql(connectorId: String, tableMetaData: TableMetaData): String {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val tableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)
    return template.replace(TABLE_PLACEHOLDER.toRegex(), tableName)
  }

  /**
   * Override this method for specific tests
   */
  open fun isApplicableOnTable(tableMetaData: TableMetaData): Boolean = true

  companion object {
    const val TABLE_PLACEHOLDER = "@TABLE@"
  }
}
