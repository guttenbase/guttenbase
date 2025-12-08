package io.github.guttenbase.configuration

import io.github.guttenbase.repository.ConnectorRepository

/**
 * Implementation for Derby data base. Derby does not support disabling (FK) constraints temporarily.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
open class DerbyTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
//  private val tableMetaData = ArrayList<TableMetaData>()
//
//  /**
//   * {@inheritDoc}
//   */
//  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
//    if (connection.autoCommit) {
//      connection.autoCommit = false
//    }
//
//    val tableMetaData = getTableMetaData(connectorId)
//    this.tableMetaData.clear()
//    this.tableMetaData.addAll(tableMetaData)
//    setTableForeignKeys(connection, connectorId, false)
//  }
//
//  /**
//   * {@inheritDoc}
//   */
//  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
//    setTableForeignKeys(connection, connectorId, true)
//  }
//
//  private fun getTableMetaData(connectorId: String) = connectorRepository.getDatabaseMetaData(connectorId).tableMetaData
//
//  private fun setTableForeignKeys(connection: Connection, connectorId: String, enable: Boolean) {
//    if (enable) {
//      val tables = TableOrderTool(connectorRepository.getDatabaseMetaData(connectorId)).orderTables(tableMetaData, false)
//
//      SchemaScriptCreatorTool(connectorRepository, connectorId, connectorId).createForeignKeyStatements(tables)
//    } else {
//      DropTablesTool(connectorRepository, connectorId).createDropForeignKeyStatements()
//    }.forEach {
//      executeSQL(connection, it.trim(';', ' '))
//    }
//  }
}
