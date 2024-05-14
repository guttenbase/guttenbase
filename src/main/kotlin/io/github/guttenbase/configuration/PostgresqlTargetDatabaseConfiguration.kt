package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection
import java.sql.SQLException


/**
 * Implementation for PostgreSQL data base.
 *
 *
 * Running ANALYZE after insertions is recommended: http://www.postgresql.org/docs/7.4/static/populate.html
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class PostgresqlTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
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
    for (tableMetaData in tableMetaDatas) {
      val tableNameMapper = connectorRepository.hint<TableMapper>(connectorId)
      val tableName = tableNameMapper.fullyQualifiedTableName(tableMetaData, tableMetaData.databaseMetaData)

      executeSQL(connection, "ALTER TABLE " + tableName + (if (enable) " ENABLE " else " DISABLE ") + "TRIGGER ALL;")
    }
  }
}
