package io.github.guttenbase.configuration

import io.github.guttenbase.meta.DatabaseEntityMetaData
import java.sql.Connection

/**
 * Configuration methods for source data base. Implementations may execute specific initialization code before and after operations are
 * executed.
 *
 *
 *
 * &copy; 2012-2044 tech@spree
 *
 *
 * @author M. Dahm
 */
interface SourceDatabaseConfiguration : DatabaseConfiguration {
  /**
   * Called before any real action is performed.
   */
  fun initializeSourceConnection(connection: Connection, connectorId: String)

  /**
   * Called after actions have been performed.
   */
  fun finalizeSourceConnection(connection: Connection, connectorId: String)

  /**
   * Called before a SELECT clause is executed.
   */
  fun beforeSelect(connection: Connection, connectorId: String, table: DatabaseEntityMetaData)

  /**
   * Called after a SELECT clause is executed.
   */
  fun afterSelect(connection: Connection, connectorId: String, table: DatabaseEntityMetaData)
}
