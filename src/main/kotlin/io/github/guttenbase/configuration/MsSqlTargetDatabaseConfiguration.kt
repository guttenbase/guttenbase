package io.github.guttenbase.configuration

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.SQLException

/**
 * Implementation for MS Server SQL data base.
 *
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [de.akquinet.jbosscc.guttenbase.hints.TableMapperHint]
 */
class MsSqlTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
  DefaultTargetDatabaseConfiguration(connectorRepository) {
  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    disableTableForeignKeys(connection, connectorId, getTableMetaData(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    enableTableForeignKeys(connection, connectorId, getTableMetaData(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun beforeInsert(connection: Connection, connectorId: String, table: TableMetaData) {
    setIdentityInsert(connection, connectorId, true, table)
  }

  /**
   * {@inheritDoc}
   */
  @Throws(SQLException::class)
  override fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData) {
    setIdentityInsert(connection, connectorId, false, table)
  }

  private fun getTableMetaData(connectorId: String): List<TableMetaData> {
    return connectorRepository.getDatabaseMetaData(connectorId).tableMetaData
  }

  @Throws(SQLException::class)
  private fun disableTableForeignKeys(connection: Connection, connectorId: String, tableMetaData: List<TableMetaData>) {
    setTableForeignKeys(connection, connectorId, tableMetaData, false)
  }

  @Throws(SQLException::class)
  private fun enableTableForeignKeys(connection: Connection, connectorId: String, tableMetaData: List<TableMetaData>) {
    setTableForeignKeys(connection, connectorId, tableMetaData, true)
  }

  @Throws(SQLException::class)
  private fun setTableForeignKeys(
    connection: Connection, connectorId: String, tableMetaDatas: List<TableMetaData>,
    enable: Boolean
  ) {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value

    for (tableMetaData in tableMetaDatas) {
      val tableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)
      executeSQL(connection, "ALTER TABLE " + tableName + if (enable) " CHECK CONSTRAINT ALL" else " NOCHECK CONSTRAINT ALL")
    }
  }

  @Throws(SQLException::class)
  private fun setIdentityInsert(
    connection: Connection, connectorId: String, enable: Boolean,
    tableMetaData: TableMetaData
  ) {
    val tableMapper: TableMapper = connectorRepository.getConnectorHint(connectorId, TableMapper::class.java).value
    val tableName: String = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)

    if (hasIdentityColumn(tableMetaData)) {
      executeSQL(connection, "SET IDENTITY_INSERT " + tableName + " " + if (enable) "ON" else "OFF")
    }
  }

  private fun hasIdentityColumn(tableMetaData: TableMetaData) = tableMetaData.columnMetaData.any { isIdentityColumn(it) }

  private fun isIdentityColumn(columnMetaData: ColumnMetaData): Boolean {
    return columnMetaData.columnTypeName.uppercase()
      .contains("IDENTITY") || columnMetaData.isPrimaryKey && columnMetaData.isAutoIncrement
        && columnMetaData.tableMetaData.primaryKeyColumns.size == 1
  }
}
