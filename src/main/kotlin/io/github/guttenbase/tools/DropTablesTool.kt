package io.github.guttenbase.tools

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.hints.TableOrderHint.Companion.getSortedTables
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.utils.Util
import java.sql.SQLException

/**
 * Will drop tables in given schema. USE WITH CARE!
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
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
    val statements = ArrayList<String>()
    val connectionInfo = connectorRepository.getConnectionInfo(connectorId)
    val constraintClause = getConstraintClause(connectionInfo)

    for (table in tableMetaData) {
      for (foreignKey in table.importedForeignKeys) {
        statements.add(
          "ALTER TABLE " + tableMapper.fullyQualifiedTableName(
            table, table.databaseMetaData
          ) + " DROP" + constraintClause + foreignKey.foreignKeyName + ";"
        )
      }
    }

    return statements
  }

  fun createDropIndexesStatements(connectorId: String): List<String> {
    val tableMetaData = TableOrderTool().getOrderedTables(
      getSortedTables(connectorRepository, connectorId), false
    )
    val connectionInfo = connectorRepository.getConnectionInfo(connectorId)
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val postgresql = connectionInfo.databaseType === DatabaseType.POSTGRESQL

    return tableMetaData.map { table ->
      val schemaPrefix = table.databaseMetaData.schemaPrefix
      val fullTableName = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)

      table.indexes.filter { !it.isPrimaryKeyIndex }.map {
        val fullIndexName = schemaPrefix + it.indexName
        val existsClause = if (postgresql) "IF EXISTS" else ""
        val constraintClause = if (postgresql && it.isUnique) POSTGRES_CONSTRAINT_DROP else DEFAULT_INDEX_DROP

        constraintClause
          .replace("@@EXISTS@@", existsClause).replace("@@INDEX_NAME@@", it.indexName)
          .replace("@@FULL_INDEX_NAME@@", fullIndexName).replace("@@FULL_TABLE_NAME@@", fullTableName)
      }
    }.flatten()
  }

  fun createDropAll(connectorId: String) =
    createDropIndexesStatements(connectorId).plus(createDropForeignKeyStatements(connectorId))
      .plus(createDropTableStatements(connectorId))

  fun createDropTableStatements(connectorId: String) =
    createTableStatements(connectorId, "DROP TABLE", dropTablesSuffix)

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
      DatabaseType.MARIADB, DatabaseType.MYSQL -> " FOREIGN KEY "
      DatabaseType.POSTGRESQL -> " CONSTRAINT IF EXISTS "
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
    private const val POSTGRES_CONSTRAINT_DROP =
      "ALTER TABLE @@FULL_TABLE_NAME@@ DROP CONSTRAINT @@EXISTS@@ @@INDEX_NAME@@;"
  }
}
