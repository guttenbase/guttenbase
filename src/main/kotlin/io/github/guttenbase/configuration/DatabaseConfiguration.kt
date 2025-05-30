package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import java.sql.Connection

/**
 * Implementations may execute specific initialization code before and after operations are executed.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface DatabaseConfiguration {
  /**
   * Called before table is copied
   */
  fun beforeTableCopy(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called after table has been copied
   */
  fun afterTableCopy(connection: Connection, connectorId: String, table: TableMetaData)
}
