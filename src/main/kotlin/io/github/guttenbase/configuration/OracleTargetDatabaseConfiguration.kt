package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import org.slf4j.LoggerFactory
import java.sql.Connection
import java.sql.SQLException


/**
 * Implementation for Oracle data base.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class OracleTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }
    setReferentialIntegrity(connection, connectorId, getTableMetaData(connectorId), false)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, connectorId, getTableMetaData(connectorId), true)
  }

  private fun getTableMetaData(connectorId: String): List<TableMetaData> {
    return TableOrderHint.getSortedTables(connectorRepository, connectorId)
  }

  @Throws(SQLException::class)
  private fun setReferentialIntegrity(
    connection: Connection, connectorId: String, tableMetaDatas: List<TableMetaData>,
    enable: Boolean
  ) {
    val tablesList = createTablesList(tableMetaDatas)

    if (tablesList.isNotBlank()) {
      val schema: String = connectorRepository.getConnectionInfo(connectorId).schema

      /* I want to disable all constraints in tables that reference the tables that I will update. */
      val foreignKeyNames: List<Map<String, Any>> = ScriptExecutorTool(connectorRepository).executeQuery(
        connectorId,
        "SELECT DISTINCT AC.OWNER, AC.TABLE_NAME, AC.CONSTRAINT_NAME FROM ALL_CONSTRAINTS AC, ALL_CONS_COLUMNS ACC "
            + "WHERE AC.CONSTRAINT_TYPE = 'R' " //
            + "AND ACC.TABLE_NAME IN (" + tablesList + ") " //
            + "AND ACC.OWNER = '" + schema + "' " //
            + "AND ACC.CONSTRAINT_NAME = AC.R_CONSTRAINT_NAME " //
            + "AND ACC.OWNER = AC.R_OWNER"
      )

      // memorize any problems that occur during constraint handling
      val problems = StringBuilder()
      for (fkMap in foreignKeyNames) {
        val tableName = fkMap["TABLE_NAME"].toString()
        val constraintName = fkMap["CONSTRAINT_NAME"].toString()
        val owner = fkMap["OWNER"].toString()
        val sql =
          "ALTER TABLE " + owner + "." + tableName + (if (enable) " ENABLE " else " DISABLE ") + "CONSTRAINT " + constraintName

        try {
          executeSQL(connection, sql)
        } catch (e: SQLException) {
          LOG.error("Unable to handle constraint: $sql", e)
          problems.append("Unable to handle constraint: ").append(sql).append("->").append(e.message).append(":")
            .append(e.sqlState)
        }
      }

      if (problems.isNotEmpty()) {
        // if there has been a problem with any constraint, now throw the exception
        throw SQLException("Constraint problems occurred: $problems")
      }
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(OracleTargetDatabaseConfiguration::class.java)

    private fun createTablesList(tableMetaDatas: List<TableMetaData>) =
      tableMetaDatas.joinToString(separator = ", ", prefix = "'", postfix = "'")
  }
}
