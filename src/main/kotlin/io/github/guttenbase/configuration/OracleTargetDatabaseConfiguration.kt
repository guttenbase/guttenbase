package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.RESULT_LIST
import io.github.guttenbase.tools.ScriptExecutorTool
import java.sql.Connection


/**
 * Implementation for Oracle data base.
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
open class OracleTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }
    setReferentialIntegrity(connection, connectorId, getTableMetaData(connectorId), false)
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setReferentialIntegrity(connection, connectorId, getTableMetaData(connectorId), true)
  }

  private fun getTableMetaData(connectorId: String): List<TableMetaData> {
    return TableOrderHint.getSortedTables(connectorRepository, connectorId)
  }

  private fun setReferentialIntegrity(
    connection: Connection, connectorId: String, tableMetaDatas: List<TableMetaData>,
    enable: Boolean
  ) {
    val tablesList = createTablesList(tableMetaDatas)

    if (tablesList.isNotBlank()) {
      val schema = connectorRepository.getConnectionInfo(connectorId).schema

      /* I want to disable all constraints in tables that reference the tables that I will update. */
      val foreignKeyNames: RESULT_LIST = ScriptExecutorTool(connectorRepository).executeQuery(
        connectorId,
        """SELECT DISTINCT AC.OWNER, AC.TABLE_NAME, AC.CONSTRAINT_NAME FROM ALL_CONSTRAINTS AC, ALL_CONS_COLUMNS ACC 
          |WHERE AC.CONSTRAINT_TYPE = 'R' AND ACC.TABLE_NAME IN $tablesList AND ACC.OWNER = '$schema' 
          |AND ACC.CONSTRAINT_NAME = AC.R_CONSTRAINT_NAME AND ACC.OWNER = AC.R_OWNER""".trimMargin()
      )

      for (fkMap in foreignKeyNames) {
        val tableName = fkMap["TABLE_NAME"].toString()
        val constraintName = fkMap["CONSTRAINT_NAME"].toString()
        val owner = fkMap["OWNER"].toString()
        val sql = """ALTER TABLE $owner."$tableName"""" +
            (if (enable) " ENABLE " else " DISABLE ") +
            """CONSTRAINT "$constraintName""""

        executeSQL(connection, sql)
      }
    }
  }

  companion object {
    private fun createTablesList(tableMetaDatas: List<TableMetaData>) =
      tableMetaDatas.joinToString(separator = ", ", prefix = "(", postfix = ")") { "'${it.tableName}'" }
  }
}
