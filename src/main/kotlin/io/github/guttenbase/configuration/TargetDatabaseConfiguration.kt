package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import java.sql.Connection
import java.sql.SQLException

/**
 * Configuration methods for target data base. Implementations may execute specific initialization code before and after operations are
 * executed.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
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
  @Throws(SQLException::class)
  fun initializeTargetConnection(connection: Connection, connectorId: String)

  /**
   * Called after actions have been performed.
   *
   *
   * Implementing classes usually re-enable foreign key and other constraints.
   */
  @Throws(SQLException::class)
  fun finalizeTargetConnection(connection: Connection, connectorId: String)

  /**
   * Called before an INSERT clause is executed. E.g., in order to disable foreign key constraints. Note that an INSERT statement may have
   * multiple VALUES clauses.
   */
  @Throws(SQLException::class)
  fun beforeInsert(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called after an INSERT clause is executed. E.g., in order to re-enable foreign key constraints.
   */
  @Throws(SQLException::class)
  fun afterInsert(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called before a new row of data (VALUES clause) is added to the INSERT statement. Note that an INSERT statement may have multiple
   * VALUES clauses. This method will be called for every VALUES clause.
   */
  @Throws(SQLException::class)
  fun beforeNewRow(connection: Connection, connectorId: String, table: TableMetaData)

  /**
   * Called after a new row of data (VALUES clause) has been added to the INSERT statement.
   */
  @Throws(SQLException::class)
  fun afterNewRow(connection: Connection, connectorId: String, table: TableMetaData)
}
