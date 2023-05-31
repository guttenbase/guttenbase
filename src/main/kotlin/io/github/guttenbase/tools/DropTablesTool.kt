package io.github.guttenbase.tools

import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.hints.TableOrderHint.Companion.getSortedTables
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.utils.Util
import java.sql.SQLException


/**
 * Will drop tables in given schema. USE WITH CARE!
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [TableOrderHint] to determine order of tables
 */
class DropTablesTool @JvmOverloads constructor(
  val connectorRepository: ConnectorRepository,
  private val dropTablesSuffix: String = ""
) {
  fun createDropForeignKeyStatements(connectorId: String): List<String> {
    val tableMetaData: List<TableMetaData> = TableOrderTool().getOrderedTables(
      getSortedTables(connectorRepository, connectorId), false
    )
    val tableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val statements = ArrayList<String>()
    val connectionInfo: ConnectorInfo = connectorRepository.getConnectionInfo(connectorId)
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

  private fun getConstraintClause(connectionInfo: ConnectorInfo): String {
    return when (connectionInfo.databaseType) {
      DatabaseType.MARIADB, DatabaseType.MYSQL -> " FOREIGN KEY "
      DatabaseType.POSTGRESQL -> " CONSTRAINT IF EXISTS "
      else -> " CONSTRAINT "
    }
  }

  fun createDropIndexesStatements(connectorId: String): List<String> {
    val tableMetaData: List<TableMetaData> = TableOrderTool().getOrderedTables(
      getSortedTables(connectorRepository, connectorId), false
    )
    val statements = ArrayList<String>()
    val connectionInfo: ConnectorInfo = connectorRepository.getConnectionInfo(connectorId)
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val postgresql = connectionInfo.databaseType === DatabaseType.POSTGRESQL

    for (table in tableMetaData) {
      val schemaPrefix: String = table.databaseMetaData.schemaPrefix
      val fullTableName: String = tableMapper.fullyQualifiedTableName(table, table.databaseMetaData)

      for (index in table.indexes) {
        if (!index.isPrimaryKeyIndex) {
          val fullIndexName = schemaPrefix + index.indexName
          val existsClause = if (postgresql) "IF EXISTS" else ""
          val constraintClause = if (postgresql && index.isUnique) POSTGRES_CONSTRAINT_DROP else DEFAULT_INDEX_DROP

          statements.add(
            constraintClause
              .replace("@@EXISTS@@".toRegex(), existsClause)
              .replace("@@INDEX_NAME@@".toRegex(), index.indexName)
              .replace("@@FULL_INDEX_NAME@@".toRegex(), fullIndexName)
              .replace("@@FULL_TABLE_NAME@@".toRegex(), fullTableName)
          )
        }
      }
    }
    return statements
  }

  fun createDropTableStatements(connectorId: String)=createTableStatements(connectorId, "DROP TABLE", dropTablesSuffix)

  fun createDeleteTableStatements(connectorId: String)=createTableStatements(connectorId, "DELETE FROM", "")

  @Throws(SQLException::class)
  fun dropTables(targetId: String) {
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, true, createDropTableStatements(targetId))
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
    ScriptExecutorTool(connectorRepository).executeScript(targetId, true, false, createDropForeignKeyStatements(targetId))
  }

  private fun createTableStatements(connectorId: String, clausePrefix: String, clauseSuffix: String): List<String> {
    val tableMetaData = TableOrderTool().getOrderedTables(      getSortedTables(connectorRepository, connectorId), false    )
    val tableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val suffix = if ("" == Util.trim(clauseSuffix)) "" else " $clauseSuffix"

    return tableMetaData.map { clausePrefix + " " + tableMapper.fullyQualifiedTableName(it, it.databaseMetaData) + suffix + ";" }
  }

  companion object {
    private const val DEFAULT_INDEX_DROP = "DROP INDEX @@EXISTS@@ @@FULL_INDEX_NAME@@;"
    private const val POSTGRES_CONSTRAINT_DROP = "ALTER TABLE @@FULL_TABLE_NAME@@ DROP CONSTRAINT @@EXISTS@@ @@INDEX_NAME@@;"
  }
}
