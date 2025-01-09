package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import java.sql.Connection

/**
 * Configuration methods for target data base. Implementations may execute specific initialization code before and after operations are
 * executed.
 *
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface TargetDatabaseConfiguration : DatabaseConfiguration {
  /**
   * If running within a container managed transaction such as within an EJB we cannot call commit() on the connection. This method should
   * return false then.
   */
  val isMayCommit: Boolean

  /**
   * Called before any action is performed which may alter the state of the target data base.
   *
   *
   * Implementing classes usually disable foreign key and other constraints temporarily.
   */
  fun initializeTargetConnection(connection: Connection, connectorId: String)

  /**
   * Called after actions have been performed.
   *
   *
   * Implementing classes usually re-enable foreign key and other constraints.
   */
  fun finalizeTargetConnection(connection: Connection, connectorId: String)

  /**
   * Called before an INSERT clause is executed. E.g., in order to disable foreign key constraints. Note that an INSERT statement may have
   * multiple VALUES clauses.
   */
  fun beforeInsert(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called after an INSERT clause is executed. E.g., in order to re-enable foreign key constraints.
   */
  fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called before a new row of data (VALUES clause) is added to the INSERT statement. Note that an INSERT statement may have multiple
   * VALUES clauses. This method will be called for every VALUES clause.
   */
  fun beforeNewRow(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called after a new row of data (VALUES clause) has been added to the INSERT statement.
   */
  fun afterNewRow(connection: Connection, connectorId: String, table: TableMetaData)
}
