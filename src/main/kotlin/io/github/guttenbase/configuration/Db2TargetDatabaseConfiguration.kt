package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import java.sql.Connection
import java.sql.SQLException
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

/**
 * Implementation for IBM DB2 data base.
 *
 * @author M. Dahm
 * @see [http://stackoverflow.com/questions/421518/is-there-a-way-to-enable-disable-constraints-in-db2-v7](http://stackoverflow.com/questions/421518/is-there-a-way-to-enable-disable-constraints-in-db2-v7)
 *
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
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    val databaseMetaData = connectorRepository.getDatabaseMetaData(connectorId)
    val tableMetaDatas: List<TableMetaData> = TableOrderHint.getSortedTables(connectorRepository, connectorId)

    schemaName = databaseMetaData.schema
    constraintsOfTable.clear()

    for (tableMetaData in tableMetaDatas) {
      constraintsOfTable[tableMetaData.tableName.uppercase()] = ArrayList()
    }

    loadConstraints(connection)
    setTableForeignKeys(connection, false)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    setTableForeignKeys(connection, true)
  }

  @Throws(SQLException::class)
  private fun setTableForeignKeys(connection: Connection, enable: Boolean) {
    for ((tableName, value) in constraintsOfTable) {
      for (constraintName in value) {
        executeSQL(
          connection, "ALTER TABLE " + schemaName
              + "."
              + tableName
              + " ALTER FOREIGN KEY "
              + constraintName
              + if (enable) " ENFORCED" else " NOT ENFORCED"
        )
      }
    }
  }

  @Throws(SQLException::class)
  private fun loadConstraints(connection: Connection) {
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
