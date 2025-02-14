package io.github.guttenbase.configuration

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableMapper
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection


/**
 * Implementation for PostgreSQL data base.
 *
 *
 * Running ANALYZE after insertions is recommended: http://www.postgresql.org/docs/7.4/static/populate.html
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class PostgresqlTargetDatabaseConfiguration(connectorRepository: ConnectorRepository) :
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
    val tableNameMapper = connectorRepository.hint<TableMapper>(connectorId)
    val sqls = tableMetaDatas.map {
      val tableName = tableNameMapper.fullyQualifiedTableName(it, it.database)

      "ALTER TABLE " + tableName + (if (enable) " ENABLE " else " DISABLE ") + "TRIGGER ALL;"
    }

    executeSQL(connection, *sqls.toTypedArray())
  }
}
