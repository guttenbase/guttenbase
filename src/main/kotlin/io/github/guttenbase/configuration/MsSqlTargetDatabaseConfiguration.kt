package io.github.guttenbase.configuration

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection
import java.sql.SQLException

/**
 * Implementation for MS Server SQL data base.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.TableMapperHint]
 */
open class MsSqlTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
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
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    enableTableForeignKeys(connection, connectorId, getTableMetaData(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  override fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    setIdentityInsert(connection, connectorId, true, table)
  }

  /**
   * {@inheritDoc}
   */
  override fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData) {
    setIdentityInsert(connection, connectorId, false, table)
  }

  private fun getTableMetaData(connectorId: String): List<TableMetaData> {
    return connectorRepository.getDatabaseMetaData(connectorId).tableMetaData
  }

  private fun disableTableForeignKeys(connection: Connection, connectorId: String, tableMetaData: List<TableMetaData>) {
    setTableForeignKeys(connection, connectorId, tableMetaData, false)
  }

  private fun enableTableForeignKeys(connection: Connection, connectorId: String, tableMetaData: List<TableMetaData>) {
    setTableForeignKeys(connection, connectorId, tableMetaData, true)
  }

  private fun setTableForeignKeys(
    connection: Connection, connectorId: String, tableMetaDatas: List<TableMetaData>, enable: Boolean
  ) {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val sqls = tableMetaDatas.map {
      val tableName = tableMapper.fullyQualifiedTableName(it, it.databaseMetaData)
      val flag = if (enable) " CHECK CONSTRAINT ALL" else " NOCHECK CONSTRAINT ALL"

      "ALTER TABLE $tableName$flag"
    }

    executeSQL(connection, *sqls.toTypedArray())
  }

  private fun setIdentityInsert(
    connection: Connection, connectorId: String, enable: Boolean,
    tableMetaData: TableMetaData
  ) {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)

    if (hasIdentityColumn(tableMetaData)) {
      val flag = if (enable) "ON" else "OFF"

      executeSQL(connection, "SET IDENTITY_INSERT $tableName $flag")
    }
  }

  private fun hasIdentityColumn(tableMetaData: TableMetaData) =
    tableMetaData.columnMetaData.any { isIdentityColumn(it) }

  private fun isIdentityColumn(columnMetaData: ColumnMetaData) =
    columnMetaData.columnTypeName.uppercase().contains("IDENTITY")
        || (columnMetaData.isPrimaryKey && columnMetaData.isAutoIncrement
        && columnMetaData.tableMetaData.primaryKeyColumns.size == 1)
}
