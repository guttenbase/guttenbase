package io.github.guttenbase.tools

import io.github.guttenbase.configuration.TargetDatabaseConfiguration
import io.github.guttenbase.progress.ScriptExecutorProgressIndicator
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.hint
import io.github.guttenbase.sql.SQLLexer
import io.github.guttenbase.utils.Util
import io.github.guttenbase.utils.Util.ARROW
import org.slf4j.LoggerFactory
import java.nio.charset.Charset
import java.sql.*

/**
 * Execute given SQL script or single statements separated by given delimiter. Delimiter is ';' by default.
 *
 * @author M. Dahm
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
open class ScriptExecutorTool
@JvmOverloads
constructor(
  private val connectorRepository: ConnectorRepository,
  private val delimiter: Char = ';',
  private val encoding: Charset = DEFAULT_ENCODING
) {
  private lateinit var progressIndicator: ScriptExecutorProgressIndicator

  /**
   * Read SQL from file somewhere on class path. Each statement (not line!) must end with a ';'
   */
  @Throws(SQLException::class)
  @JvmOverloads
  fun executeFileScript(
    connectorId: String,
    updateSchema: Boolean = true,
    prepareTargetConnection: Boolean = true,
    resourceName: String
  ) {
    executeScript(connectorId, updateSchema, prepareTargetConnection, Util.readLinesFromFile(resourceName, encoding))
  }

  /**
   * Execute given lines of SQL. Each statement (not line!) must end with a ';'
   */
  @Throws(SQLException::class)
  @JvmOverloads
  fun executeScript(
    connectorId: String,
    updateSchema: Boolean = true,
    prepareTargetConnection: Boolean = true,
    vararg lines: String
  ) {
    executeScript(connectorId, updateSchema, prepareTargetConnection, listOf(*lines))
  }

  /**
   * Execute given lines of SQL. Each statement (not line!) must end with a ';'
   *
   * @param connectorId
   * @param scriptUpdatesSchema     The script alters the schema, schema information needs to be reloaded
   * @param prepareTargetConnection the target connection is initialized using the appropriate [TargetDatabaseConfiguration]
   * @param lines                   SQL statements ending with ';'
   * @throws SQLException
   */
  @Throws(SQLException::class)
  @JvmOverloads
  fun executeScript(
    connectorId: String, scriptUpdatesSchema: Boolean = true, prepareTargetConnection: Boolean = true,
    lines: List<String>
  ) {
    if (lines.isNotEmpty()) {
      val targetDatabaseConfiguration = connectorRepository.getTargetDatabaseConfiguration(connectorId)
      val sqlStatements = SQLLexer(lines, delimiter).parse()

      progressIndicator = connectorRepository.hint<ScriptExecutorProgressIndicator>(connectorId)
      progressIndicator.initializeIndicator()

      connectorRepository.createConnector(connectorId).use { connector ->
        connector.openConnection().use { connection ->
          connection.createStatement().use { statement ->
            if (prepareTargetConnection) {
              targetDatabaseConfiguration.initializeTargetConnection(connection, connectorId)
            }

            if (connection.autoCommit) {
              connection.autoCommit = false
            }

            progressIndicator.startProcess(sqlStatements.size)

            for (sql in sqlStatements) {
              progressIndicator.startExecution(sql)
              try {
                executeSQL(statement, sql)
              } catch (e: Exception) {
                LOG.error(
                  """|
                  |Error in "$sql" 
                  |$ARROW ${e.message}
                  """.trimMargin(), e
                )
                throw e
              } finally {
                progressIndicator.endExecution(1)
                progressIndicator.endProcess()
              }
            }

            if (targetDatabaseConfiguration.isMayCommit) {
              connection.commit()
            }

            if (scriptUpdatesSchema) {
              connectorRepository.refreshDatabaseMetaData(connectorId)
            }

            if (prepareTargetConnection) {
              targetDatabaseConfiguration.finalizeTargetConnection(connection, connectorId)
            }

            if (scriptUpdatesSchema) {
              connectorRepository.refreshDatabaseMetaData(connectorId)
            }
          }
        }
      }

      progressIndicator.finalizeIndicator()
    }
  }

  /**
   * Execute query (i.e. SELECT...) and return the result set as a list of Maps where the key is the column name and the value the
   * respective data.
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  fun executeQuery(connectorId: String, sql: String): RESULT_LIST {
    connectorRepository.createConnector(connectorId).use { connector ->
      val connection: Connection = connector.openConnection()
      return executeQuery(connection, sql)
    }
  }

  /**
   * Execute query (i.e. SELECT...) and execute the given command on each row of data
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  fun executeQuery(connectorId: String, sql: String, action: Command) {
    connectorRepository.createConnector(connectorId).use { connector ->
      val connection: Connection = connector.openConnection()
      executeQuery(connection, sql, action)
    }
  }

  /**
   * Execute query (i.e. SELECT...) and return the result set as a list of Maps where the key is the upper case column name
   * and the value is the respective data of the column.
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  fun executeQuery(connection: Connection, sql: String): RESULT_LIST {
    val result: MutableList<RESULT_MAP> = ArrayList()

    connection.createStatement().use { statement ->
      statement.executeQuery(sql).use { resultSet ->
        readMapFromResultSet(connection, resultSet) { _: Connection, data: RESULT_MAP -> result.add(data) }
      }
    }

    return result
  }

  /**
   * Execute query (i.e. SELECT...) and execute the given command on each row of data
   *
   * @throws SQLException
   */
  @Throws(SQLException::class)
  fun executeQuery(connection: Connection, sql: String, action: Command) {
    connection.createStatement()
      .use { statement ->
        statement.executeQuery(sql).use { resultSet -> readMapFromResultSet(connection, resultSet, action) }
      }
  }

  @Throws(SQLException::class)
  private fun readMapFromResultSet(connection: Connection, resultSet: ResultSet, action: Command) {
    val metaData = resultSet.metaData
    action.initialize(connection)

    while (resultSet.next()) {
      val map = HashMap<String, Any?>()

      for (i in 1..metaData.columnCount) {
        val columnName = metaData.getColumnName(i).uppercase()
        val value = resultSet.getObject(i)

        map[columnName] = value
      }

      action.execute(connection, map)
    }

    action.finalize(connection)
  }

  private fun executeSQL(statement: Statement, sql: String) {
    progressIndicator.info("Executing: $sql")

    if (statement.execute(sql)) {
      statement.resultSet.use { resultSet ->
        readMapFromResultSet(statement.connection, resultSet) { _, map: RESULT_MAP ->
          progressIndicator.info("Query result: $map")
        }
      }
    } else {
      val updateCount = statement.updateCount
      progressIndicator.info("Update count: $updateCount")
    }
  }

  fun interface Command {
    /**
     * Called before first execution
     *
     * @param connection
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun initialize(connection: Connection) {
    }

    /**
     * Called after last execution
     *
     * @param connection
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun finalize(connection: Connection) {
    }

    /**
     * Executed for each row of data
     *
     * @param connection
     * @param data
     * @throws SQLException
     */
    @Throws(SQLException::class)
    fun execute(connection: Connection, data: RESULT_MAP)
  }

  abstract class StatementCommand protected constructor(private val sql: String) : Command {
    protected lateinit var statement: PreparedStatement

    @Throws(SQLException::class)
    override fun initialize(connection: Connection) {
      statement = connection.prepareStatement(sql)
    }

    @Throws(SQLException::class)
    override fun finalize(connection: Connection) {
      statement.close()
    }
  }

  companion object {
    @JvmStatic
    private val LOG = LoggerFactory.getLogger(ScriptExecutorTool::class.java)

    val DEFAULT_ENCODING: Charset = Charset.defaultCharset()
  }
}

typealias RESULT_MAP = Map<String, Any?>
typealias RESULT_LIST = List<RESULT_MAP>
