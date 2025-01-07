package io.github.guttenbase.tools.postgresql

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractSequenceUpdateTool


/**
 * Usually Postgresql creates an autoincrement ID sequence for tables. After data migration these sequences need to be updated...
 *
 * By default the sequence is updated to SELECT(MAX(ID) + 1) FROM table
 *
 * &copy; 2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
open class PostgresqlSequenceUpdateTool(connectorRepository: ConnectorRepository) : AbstractSequenceUpdateTool(connectorRepository) {
  override fun getSequenceName(tableName: String) = tableName + "_id_seq"

  override fun getUpdateSequenceClause(sequenceName: String, sequenceValue: Long) =
    "SELECT setval('$sequenceName', $sequenceValue, true);"
}
