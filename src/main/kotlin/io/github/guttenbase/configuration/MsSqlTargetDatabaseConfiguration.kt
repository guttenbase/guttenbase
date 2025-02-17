package io.github.guttenbase.configuration

import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection

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
  override fun initializeTargetConnection(connection: Connection, connectorId: String) {
    if (connection.autoCommit) {
      connection.autoCommit = false
    }

    disableTableForeignKeys(connection, connectorId, getTable(connectorId))
  }

  /**
   * {@inheritDoc}
   */
  override fun finalizeTargetConnection(connection: Connection, connectorId: String) {
    enableTableForeignKeys(connection, connectorId, getTable(connectorId))
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

  private fun getTable(connectorId: String) = connectorRepository.getDatabase(connectorId).tables

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
      val tableName = tableMapper.fullyQualifiedTableName(it, it.database)
      val flag = if (enable) "CHECK CONSTRAINT ALL" else "NOCHECK CONSTRAINT ALL"

      """ALTER TABLE $tableName $flag"""
    }

    executeSQL(connection, *sqls.toTypedArray())
  }

  private fun setIdentityInsert(
    connection: Connection, connectorId: String, enable: Boolean,
    tableMetaData: TableMetaData
  ) {
    val tableMapper = connectorRepository.hint<TableMapper>(connectorId)
    val tableName = tableMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.database)

    if (hasIdentityColumn(tableMetaData)) {
      val flag = if (enable) "ON" else "OFF"

      executeSQL(connection, """SET IDENTITY_INSERT $tableName $flag""")
    }
  }

  private fun hasIdentityColumn(tableMetaData: TableMetaData) =
    tableMetaData.columns.any { isIdentityColumn(it) }

  private fun isIdentityColumn(columnMetaData: ColumnMetaData) =
    columnMetaData.columnTypeName.uppercase().contains("IDENTITY")
        || (columnMetaData.isPrimaryKey && columnMetaData.isAutoIncrement
        && columnMetaData.table.primaryKeyColumns.size == 1)
}
