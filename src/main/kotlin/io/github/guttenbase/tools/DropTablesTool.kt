package io.github.guttenbase.tools

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.tools.ScriptExecutorTool.Companion.executeScriptWithRetry
import io.github.guttenbase.utils.Util
import java.sql.SQLException

/**
 * Will drop tables in given schema. USE WITH CARE!
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 * Hint is used by [TableOrderHint] to determine order of tables
 */
@Suppress("MemberVisibilityCanBePrivate")
open class DropTablesTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String
) {
  private val databaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(connectorId) }
  private val tableMetaData get() = TableOrderTool(databaseMetaData).orderTables(false)
  private val dropTablesSuffix = databaseMetaData.databaseType.dropTablesSuffix

  fun createDropForeignKeyStatements(): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val connectionInfo = connectorRepository.getConnectionInfo(connectorId)
    val constraintClause = getConstraintClause(connectionInfo)
    val existsClause = connectorRepository.getConnectionInfo(connectorId).databaseType.constraintExistsClause

    return tableMetaData.map { table ->
      table.importedForeignKeys.map {
        val fullTableName = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)

        DEFAULT_CONSTRAINT_DROP.replace(FULL_TABLE_NAME, fullTableName)
          .replace(IF_EXISTS, existsClause)
          .replace(CONSTRAINT, constraintClause).replace(FK_NAME, it.foreignKeyName)
      }
    }.flatten()
  }

  fun createDropIndexStatements(): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val ifExistsClause = connectorRepository.getConnectionInfo(connectorId).databaseType.indexExistsClause

    return tableMetaData.map { table ->
      val schemaPrefix = table.databaseMetaData.schemaPrefix
      val fullTableName = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)

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

  private fun chooseDropIndexClause(index: IndexMetaData) = when (index.tableMetaData.databaseMetaData.databaseType) {
    MARIADB, MYSQL -> MYSQL_INDEX_DROP
    MSSQL -> MSSQL_INDEX_DROP
    else -> DEFAULT_INDEX_DROP
  }

  fun createDropAll() = createDropIndexStatements().plus(createDropForeignKeyStatements())
    .plus(createDropTableStatements())

  fun createDropTableStatements() =
    createTableStatements(
      "DROP TABLE" + (" " + connectorRepository.getConnectionInfo(connectorId).databaseType.tableExistsClause).trimEnd(),
      dropTablesSuffix
    )

  fun createDeleteTableStatements() = createTableStatements("DELETE FROM", "")

  @Throws(SQLException::class)
  @JvmOverloads
  fun dropTables(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropTableStatements())
  }

  @Throws(SQLException::class)
  @JvmOverloads
  fun dropAll(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropAll())
  }

  @Throws(SQLException::class)
  fun clearTables() {
    ScriptExecutorTool(connectorRepository).executeScript(
      connectorId, true, true, createDeleteTableStatements()
    )
  }

  @Throws(SQLException::class)
  @JvmOverloads
  fun dropIndexes(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropIndexStatements())
  }

  @Throws(SQLException::class)
  @JvmOverloads
  fun dropForeignKeys(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    executeScriptWithRetry(connectorRepository, connectorId, prepareTargetConnection, retryFailed, createDropForeignKeyStatements())
  }

  private fun getConstraintClause(connectionInfo: ConnectorInfo): String {
    return when (connectionInfo.databaseType) {
      MARIADB, MYSQL -> "FOREIGN KEY"
      else -> "CONSTRAINT"
    }
  }

  private fun createTableStatements(clausePrefix: String, clauseSuffix: String): List<String> {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val suffix = if ("" == Util.trim(clauseSuffix)) "" else " $clauseSuffix"

    return tableMetaData.map {
      "$clausePrefix " + tableMapper.fullyQualifiedTableName(it, it.databaseMetaData) + suffix + ";"
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
