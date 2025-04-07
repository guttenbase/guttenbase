package io.github.guttenbase.tools

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.DatabaseEntityMetaData
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.databaseType
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.ScriptExecutorTool.Companion.executeScriptWithRetry
import io.github.guttenbase.utils.Util

/**
 * Will drop tables in given schema. USE WITH CARE!
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [TableOrderHint] to determine order of tables
 */
open class DropTablesTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String
) {
  private val databaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabase(connectorId) }
  private val tableMetaData by lazy { TableOrderTool(databaseMetaData).orderTables(topDown = false) }
  private val viewMetaData by lazy { databaseMetaData.views }
  private val dropTablesSuffix = databaseMetaData.databaseType.dropTablesSuffix

  fun createDropForeignKeyStatements(): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val connectionInfo = connectorRepository.getConnectionInfo(connectorId)
    val constraintClause = getConstraintClause(connectionInfo)
    val existsClause = connectorRepository.getConnectionInfo(connectorId).databaseType.constraintExistsClause

    return tableMetaData.map { table ->
      table.importedForeignKeys.map {
        val fullTableName = tableMapper.fullyQualifiedTableName(table, table.database)

        DEFAULT_CONSTRAINT_DROP.replace(FULL_TABLE_NAME, fullTableName)
          .replace(IF_EXISTS, existsClause)
          .replace(CONSTRAINT, constraintClause)
          .replace(FK_NAME, databaseMetaData.databaseType.escapeDatabaseEntity(it.foreignKeyName))
      }
    }.flatten()
  }

  fun createDropIndexStatements(): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val ifExistsClause = connectorRepository.getConnectionInfo(connectorId).databaseType.indexExistsClause

    return tableMetaData.map { table ->
      val schemaPrefix = table.database.schemaPrefix
      val fullTableName = tableMapper.fullyQualifiedTableName(table, table.database)

      table.indexes.filter { !it.isPrimaryKeyIndex }.map {
        val fullIndexName = schemaPrefix + it.indexName
        val fullTableAndIndexName = fullTableName + "." + it.indexName
        val dropIndexClause = chooseDropIndexClause(it)

        dropIndexClause
          .replace(IF_EXISTS, ifExistsClause).replace(INDEX_NAME, it.indexName)
          .replace(FULL_INDEX_NAME, fullIndexName).replace(FULL_TABLE_AND_INDEX_NAME, fullTableAndIndexName)
          .replace(FULL_TABLE_NAME, fullTableName)
      }
    }.flatten()
  }

  private fun chooseDropIndexClause(index: IndexMetaData) = when (index.databaseType) {
    MARIADB, MYSQL -> MYSQL_INDEX_DROP
    MSSQL -> MSSQL_INDEX_DROP
    else -> DEFAULT_INDEX_DROP
  }

  fun createDropAll() = createDropViewStatements().plus(
    createDropIndexStatements().plus(createDropForeignKeyStatements())
      .plus(createDropTableStatements())
  )

  fun createDropTableStatements() = createTableStatements(
    tableMetaData,
    "DROP TABLE" + (" " + connectorRepository.getConnectionInfo(connectorId).databaseType.tableExistsClause).trimEnd(),
    dropTablesSuffix
  )

  fun createDropViewStatements() = createTableStatements(
    viewMetaData,
    "DROP VIEW" + (" " + connectorRepository.getConnectionInfo(connectorId).databaseType.tableExistsClause).trimEnd(),
    dropTablesSuffix
  )

  fun createDeleteTableStatements() = createTableStatements(this@DropTablesTool.tableMetaData, "DELETE FROM", "")

  @JvmOverloads
  fun dropTables(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropTableStatements())
  }

  @JvmOverloads
  fun dropViews(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropViewStatements())
  }

  @JvmOverloads
  fun dropAll(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropAll())
  }

  fun clearTables() {
    ScriptExecutorTool(connectorRepository).executeScript(
      connectorId, true, true, createDeleteTableStatements()
    )
  }

  @JvmOverloads
  fun dropIndexes(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropIndexStatements())
  }

  @JvmOverloads
  fun dropForeignKeys(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(
      connectorRepository,
      connectorId,
      prepareTargetConnection,
      retryFailed,
      createDropForeignKeyStatements()
    )
  }

  private fun getConstraintClause(connectionInfo: ConnectorInfo): String {
    return when (connectionInfo.databaseType) {
      MARIADB, MYSQL -> "FOREIGN KEY"
      else -> "CONSTRAINT"
    }
  }

  private fun createTableStatements(
    tables: List<DatabaseEntityMetaData>, clausePrefix: String, clauseSuffix: String
  ): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val suffix = if ("" == Util.trim(clauseSuffix)) "" else " $clauseSuffix"

    return tables.map {
      "$clausePrefix " + tableMapper.fullyQualifiedTableName(it, it.database) + suffix + ";"
    }
  }

  companion object {
    private const val FULL_INDEX_NAME = "@@FULL_INDEX_NAME@@"
    private const val FULL_TABLE_AND_INDEX_NAME = "@@FULL_TABLE_AND_INDEX_NAME@@"
    private const val FULL_TABLE_NAME = "@@FULL_TABLE_NAME@@"
    private const val IF_EXISTS = "@@EXISTS@@"
    private const val INDEX_NAME = "@@INDEX_NAME@@"
    private const val CONSTRAINT = "@@CONSTRAINT@@"
    private const val FK_NAME = "@@FK_NAME@@"

    private const val DEFAULT_INDEX_DROP = "DROP INDEX $IF_EXISTS $FULL_INDEX_NAME;"
    private const val MSSQL_INDEX_DROP = "DROP INDEX $FULL_TABLE_AND_INDEX_NAME;"
    private const val MYSQL_INDEX_DROP = "DROP INDEX $INDEX_NAME ON $FULL_TABLE_NAME ;"
    private const val DEFAULT_CONSTRAINT_DROP =
      "ALTER TABLE $FULL_TABLE_NAME DROP $CONSTRAINT $IF_EXISTS $FK_NAME;"
  }
}
