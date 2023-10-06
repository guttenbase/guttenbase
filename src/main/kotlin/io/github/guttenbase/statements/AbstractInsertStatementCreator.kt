package io.github.guttenbase.statements

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

/**
 * Create INSERT statement with multiple VALUES tuples.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractInsertStatementCreator(connectorRepository: ConnectorRepository, connectorId: String) :
  AbstractStatementCreator(connectorRepository, connectorId) {
  /**
   * Create INSERT statement for the mapped target columns.
   */
  @Throws(SQLException::class)
  fun createInsertStatement(
    sourceConnectorId: String, sourceTableMetaData: TableMetaData,
    targetTableName: String, targetTableMetaData: TableMetaData, destConnection: Connection,
    numberOfRowsPerBatch: Int, useMultipleValuesClauses: Boolean
  ): PreparedStatement {
    assert(numberOfRowsPerBatch > 0) { "numberOfValueClauses > 0" }

    val numberOfValuesClauses = if (useMultipleValuesClauses) numberOfRowsPerBatch else 1
    val sql =
      createSQL(sourceConnectorId, sourceTableMetaData, targetTableName, targetTableMetaData, numberOfValuesClauses)

    LOG.debug("Create INSERT statement: $sql")
    return destConnection.prepareStatement(sql)
  }

  protected fun createValueTuples(numberOfValuesClauses: Int, columnCount: Int): String {
    val tuple = (1..columnCount).joinToString(prefix = "(", postfix = ")", transform = { "?" })
    return (1..numberOfValuesClauses).joinToString(transform = { tuple })
  }

  private fun createSQL(
    sourceConnectorId: String, sourceTableMetaData: TableMetaData, targetTableName: String,
    targetTableMetaData: TableMetaData, numberOfValueClauses: Int
  ): String {
    val columns = getMappedTargetColumns(sourceTableMetaData, targetTableMetaData, sourceConnectorId)

    return INSERT_INTO + targetTableName + " (" + createColumnClause(columns) + ") VALUES " +
        createValueTuples(numberOfValueClauses, columns.size)
  }

  companion object {
    const val INSERT_INTO = "INSERT INTO "
  }
}
