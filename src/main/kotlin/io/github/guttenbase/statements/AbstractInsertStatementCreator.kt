package io.github.guttenbase.statements

import io.github.guttenbase.mapping.PreparedStatementPlaceholderFactory
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import java.sql.Connection
import java.sql.PreparedStatement

/**
 * Create INSERT statement with multiple VALUES tuples.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractInsertStatementCreator(connectorRepository: ConnectorRepository, targetConnectorId: String) :
  AbstractStatementCreator(connectorRepository, targetConnectorId) {
  protected val placeholderFactory = connectorRepository.hint<PreparedStatementPlaceholderFactory>(targetConnectorId)

  /**
   * Create INSERT statement for the mapped target columns.
   */
  fun createInsertStatement(
    sourceTableMetaData: TableMetaData, targetTableName: String,
    targetTableMetaData: TableMetaData, destConnection: Connection, numberOfRowsPerBatch: Int,
    useMultipleValuesClauses: Boolean
  ): PreparedStatement {
    assert(numberOfRowsPerBatch > 0) { "numberOfValueClauses > 0" }

    val numberOfValuesClauses = if (useMultipleValuesClauses) numberOfRowsPerBatch else 1
    val columns = getMappedTargetColumns(sourceTableMetaData, targetTableMetaData)
    val insert = "INSERT INTO " + targetTableName + " (" + createColumnClause(columns) + ")"
    val sql = insert + " VALUES\n" + createValueTuples(numberOfValuesClauses, columns)

    indicator.debug("Create INSERT statement: $insert")
    return destConnection.prepareStatement(sql)
  }

  protected fun createValueTuples(numberOfValuesClauses: Int, columns: List<ColumnMetaData>): String {
    val tuple = columns.joinToString(prefix = "\t(", postfix = ")", transform = { placeholderFactory.map(it) })

    return (1..numberOfValuesClauses).joinToString(separator = ",\n", transform = { tuple })
  }
}
