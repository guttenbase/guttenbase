package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import java.sql.Connection
import java.sql.SQLException

/**
 * Implementation for IBM DB2 data base.
 *
 * @author M. Dahm
 * @see [http://stackoverflow.com/questions/421518/is-there-a-way-to-enable-disable-constraints-in-db2-v7](http://stackoverflow.com/questions/421518/is-there-a-way-to-enable-disable-constraints-in-db2-v7)
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
open class Db2TargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  private val constraintsOfTable: MutableMap<String, MutableList<String>> = LinkedHashMap()
  private var schemaName: String = ""

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    loadConstraints(connection, connectorId)
    setTableForeignKeys(connection, false)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    loadConstraints(connection, connectorId)
    setTableForeignKeys(connection, true)
  }

  @Throws(SQLException::class)
  private fun setTableForeignKeys(connection: Connection, enable: Boolean) {
    val sqls = constraintsOfTable.entries.map { e ->
      e.value.map {
        "ALTER TABLE " + schemaName + "." + e.key + " ALTER FOREIGN KEY " + it + if (enable) " ENFORCED" else " NOT ENFORCED"
      }
    }.flatten().plus("COMMIT")

    executeSQL(connection, *sqls.toTypedArray())
  }

  private fun loadConstraints(connection: Connection, connectorId: String) {
    val databaseMetaData = connectorRepository.getDatabaseMetaData(connectorId)
    val tableMetaDatas = TableOrderHint.getSortedTables(connectorRepository, connectorId)

    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    schemaName = databaseMetaData.schema
    constraintsOfTable.clear()

    for (tableMetaData in tableMetaDatas) {
      constraintsOfTable[tableMetaData.tableName.uppercase()] = ArrayList()
    }

    val scriptExecutorTool = ScriptExecutorTool(connectorRepository)
    val queryResult = scriptExecutorTool.executeQuery(
      connection,
      "SELECT DISTINCT CONSTNAME, TABNAME FROM SYSCAT.TABCONST WHERE TABSCHEMA='$schemaName' AND TYPE='F' ORDER BY TABNAME"
    )

    for (map in queryResult) {
      val constraintName = map["CONSTNAME"].toString()
      val tableName = map["TABNAME"].toString().uppercase()
      val constraintNames = constraintsOfTable[tableName]

      constraintNames?.add(constraintName)
    }
  }
}
