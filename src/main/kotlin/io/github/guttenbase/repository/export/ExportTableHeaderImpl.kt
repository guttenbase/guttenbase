package io.github.guttenbase.repository.export

import io.github.guttenbase.meta.TableMetaData

/**
 * Denote start of new table in export file
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ExportTableHeaderImpl(tableMetaData: TableMetaData) : ExportTableHeader {
  override val tableName = tableMetaData.tableName

  override fun toString()= tableName

  companion object {
    private const val serialVersionUID = 1L
  }
}
