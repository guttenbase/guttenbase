package io.github.guttenbase.export

import java.io.Serializable

/**
 * Denote start of new table in export file. Provide mininmal information about table.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ExportTableHeader : Serializable {
  val tableName: String
}
