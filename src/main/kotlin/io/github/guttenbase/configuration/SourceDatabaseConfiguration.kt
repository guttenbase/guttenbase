package io.github.guttenbase.configuration

import io.github.guttenbase.meta.TableMetaData
import java.sql.Connection
import java.sql.SQLException

/**
 * Configuration methods for source data base. Implementations may execute specific initialization code before and after operations are
 * executed.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface SourceDatabaseConfiguration : DatabaseConfiguration {
    /**
     * Called before any real action is performed.
     */
    @Throws(SQLException::class)
    fun initializeSourceConnection(connection: Connection, connectorId: String)

    /**
     * Called after actions have been performed.
     */
    @Throws(SQLException::class)
    fun finalizeSourceConnection(connection: Connection, connectorId: String)

    /**
     * Called before a SELECT clause is executed.
     */
    @Throws(SQLException::class)
    fun beforeSelect(connection: Connection, connectorId: String, table: TableMetaData)

    /**
     * Called after a SELECT clause is executed.
     */
    @Throws(SQLException::class)
    fun afterSelect(connection: Connection, connectorId: String, table: TableMetaData)
}
