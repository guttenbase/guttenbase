package io.github.guttenbase.tools

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.DatabaseType.*
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.hints.TableOrderHint.Companion.getSortedTables
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
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
open class DropTablesTool @JvmOverloads constructor(
  private val connectorRepository: ConnectorRepository,
  private val dropTablesSuffix: String = ""
) {
  fun createDropForeignKeyStatements(connectorId: String): List<String> {
    val tableMetaData = TableOrderTool().getOrderedTables(
      getSortedTables(connectorRepository, connectorId), false
    )
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val connectionInfo = connectorRepository.getConnectionInfo(connectorId)
    val constraintClause = getConstraintClause(connectionInfo)
    val existsClause = chooseExistsClause(connectorRepository.getConnectionInfo(connectorId).databaseType)

    return tableMetaData.map { table ->
      table.importedForeignKeys.map {
        val fullTableName = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)
        DEFAULT_CONSTRAINT_DROP.replace("@@FULL_TABLE_NAME@@", fullTableName)
          .replace("@@EXISTS@@", existsClause)
          .replace("@@CONSTRAINT@@", constraintClause).replace("@@FK_NAME@@", it.foreignKeyName)
      }
    }.flatten()
  }

  fun createDropIndexesStatements(connectorId: String): List<String> {
    val tableMetaData = TableOrderTool().getOrderedTables(
      getSortedTables(connectorRepository, connectorId), false
    )
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val existsClause = chooseExistsClause(connectorRepository.getConnectionInfo(connectorId).databaseType)

    return tableMetaData.map { table ->
      val schemaPrefix = table.databaseMetaData.schemaPrefix
      val fullTableName = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)

      table.indexes.filter { !it.isPrimaryKeyIndex }.map {
        val fullIndexName = schemaPrefix + it.indexName
        val dropIndexClause = chooseDropIndexClause(it)

        dropIndexClause
          .replace("@@EXISTS@@", existsClause).replace("@@INDEX_NAME@@", it.indexName)
          .replace("@@FULL_INDEX_NAME@@", fullIndexName).replace("@@FULL_TABLE_NAME@@", fullTableName)
      }
    }.flatten()
  }

  private fun chooseExistsClause(databaseType: DatabaseType) =
    when (databaseType) {
      POSTGRESQL, MYSQL -> " IF EXISTS"
      else -> ""
    }

  private fun chooseDropIndexClause(index: IndexMetaData) = when (index.tableMetaData.databaseMetaData.databaseType) {
    MYSQL -> MYSQL_INDEX_DROP
    else -> DEFAULT_INDEX_DROP
  }

  fun createDropAll(connectorId: String) =
    createDropIndexesStatements(connectorId).plus(createDropForeignKeyStatements(connectorId))
      .plus(createDropTableStatements(connectorId))

  fun createDropTableStatements(connectorId: String) =
    createTableStatements(
      connectorId, "DROP TABLE"
          + chooseExistsClause(connectorRepository.getConnectionInfo(connectorId).databaseType),
      dropTablesSuffix
    )

  fun createDeleteTableStatements(connectorId: String) = createTableStatements(connectorId, "DELETE FROM", "")

  @Throws(SQLException::class)
  fun dropTables(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, true, createDropTableStatements(targetId))
  }

  @Throws(SQLException::class)
  fun dropAll(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, true, createDropAll(targetId))
  }

  @Throws(SQLException::class)
  fun clearTables(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, true, createDeleteTableStatements(targetId))
  }

  @Throws(SQLException::class)
  fun dropIndexes(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, false, createDropIndexesStatements(targetId))
  }

  @Throws(SQLException::class)
  fun dropForeignKeys(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(
      targetId,
      true,
      false,
      createDropForeignKeyStatements(targetId)
    )
  }

  private fun getConstraintClause(connectionInfo: ConnectorInfo): String {
    return when (connectionInfo.databaseType) {
      MARIADB, MYSQL -> " FOREIGN KEY "
      POSTGRESQL -> " CONSTRAINT IF EXISTS "
      else -> " CONSTRAINT "
    }
  }

  private fun createTableStatements(connectorId: String, clausePrefix: String, clauseSuffix: String): List<String> {
    val tableMetaData = TableOrderTool().getOrderedTables(getSortedTables(connectorRepository, connectorId), false)
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val suffix = if ("" == Util.trim(clauseSuffix)) "" else " $clauseSuffix"

    return tableMetaData.map {
      "$clausePrefix " + tableMapper.fullyQualifiedTableName(it, it.databaseMetaData) + suffix + ";"
    }
  }

  companion object {
    private const val DEFAULT_INDEX_DROP = "DROP INDEX @@EXISTS@@ @@FULL_INDEX_NAME@@;"
    private const val MYSQL_INDEX_DROP = "ALTER TABLE @@FULL_TABLE_NAME@@ DROP INDEX @@EXISTS@@ @@FULL_INDEX_NAME@@;"
    private const val DEFAULT_CONSTRAINT_DROP = "ALTER TABLE @@FULL_TABLE_NAME@@ DROP @@CONSTRAINT@@ @@EXISTS@@ @@FK_NAME@@;"
  }
}
