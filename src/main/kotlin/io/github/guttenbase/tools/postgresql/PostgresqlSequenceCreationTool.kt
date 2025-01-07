package io.github.guttenbase.tools.postgresql

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.AbstractSequenceCreationTool


/**
 * Create an autoincrement ID sequence for tables.
 *
 *
 * &copy; 2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class PostgresqlSequenceCreationTool(connectorRepository: ConnectorRepository) :
  AbstractSequenceCreationTool(connectorRepository) {
  override fun getIdColumn(tableMetaData: TableMetaData) = "ID"

  override fun getCreateSequenceClauses(
    tableName: String,
    idColumn: String,
    sequenceName: String,
    start: Long,
    incrementBy: Long
  ) = listOf(
    """CREATE SEQUENCE $sequenceName    START WITH $start    INCREMENT BY $incrementBy    NO MINVALUE
    NO MAXVALUE
    CACHE 1;""",
    "ALTER SEQUENCE $sequenceName OWNED BY $tableName.$idColumn;"
  )


  override fun getSequenceName(tableName: String) = tableName + "_id_seq"
}
