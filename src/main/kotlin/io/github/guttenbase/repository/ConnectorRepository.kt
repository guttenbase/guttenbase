package io.github.guttenbase.repository

import io.github.guttenbase.configuration.*
import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.defaults.impl.DefaultDatabaseForeignKeyFilter
import io.github.guttenbase.defaults.impl.DefaultDatabaseIndexFilter
import io.github.guttenbase.export.plain.ExportSQLConnectorInfo
import io.github.guttenbase.hints.*
import io.github.guttenbase.hints.impl.*
import io.github.guttenbase.mapping.ForeignKeyMapper
import io.github.guttenbase.mapping.IndexMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.meta.DatabaseType.*
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import java.sql.SQLException
import java.util.*

/**
 * The main repository containing all configured connectors.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class ConnectorRepository {
  private val connectionInfoMap = TreeMap<String, ConnectorInfo>()
  private val sourceDatabaseConfigurationMap = HashMap<DatabaseType, SourceDatabaseConfiguration>()
  private val targetDatabaseConfigurationMap = HashMap<DatabaseType, TargetDatabaseConfiguration>()

  /**
   * Cache metadata since some databases are very slow retrieving.
   */
  private val databaseMap = HashMap<String, InternalDatabaseMetaData>()
  private val connectionHintMap = HashMap<String, MutableMap<Class<*>, ConnectorHint<*>>>()

  init {
    initDefaultConfiguration()
  }

  /**
   * Add connection info to repository with symbolic ID for data base such as "source db", e.g.
   */
  open fun addConnectionInfo(connectorId: String, connectionInfo: ConnectorInfo): ConnectorRepository {
    connectionInfoMap[connectorId] = connectionInfo
    initDefaultHints(connectorId)
    return this
  }

  /**
   * Remove all information about connector
   */
  @Suppress("unused")
  open fun removeConnectionInfo(connectorId: String): ConnectorRepository {
    connectionInfoMap.remove(connectorId)
    connectionHintMap.remove(connectorId)
    databaseMap.remove(connectorId)

    return this
  }

  /**
   * Add configuration hint for connector, if you pass "null" as connector ID, the hint will be applied to all connectors present
   */
  open fun <T> addConnectorHint(connectorId: String?, hint: ConnectorHint<T>): ConnectorRepository {
    val ids = if (connectorId == null) connectionInfoMap.keys else setOf(connectorId)

    ids.forEach {
      // Check connector if is configured
      getConnectionInfo(it)
      val hintMap = connectionHintMap.getOrPut(it) { HashMap() }
      hintMap[hint.connectorHintType] = hint

      refreshDatabase(it)
    }

    return this
  }

  /**
   * Get configuration hint for connector
   */
  @Suppress("UNCHECKED_CAST")
  open fun <T> getConnectorHint(connectorId: String, connectorHintType: Class<T>): ConnectorHint<T> {
    val hintMap = connectionHintMap[connectorId] ?: throw IllegalStateException("No hints defined for $connectorId")
    return hintMap[connectorHintType] as ConnectorHint<T>
  }

  /**
   * Get connection info for connector
   */
  open fun getConnectionInfo(connectorId: String) =
    connectionInfoMap[connectorId] ?: throw IllegalStateException("Connector not configured: $connectorId")

  /**
   * Get all meta data from data base.
   */
  open fun getDatabase(connectorId: String): DatabaseMetaData {
    return try {
      var databaseMetaData: InternalDatabaseMetaData? = databaseMap[connectorId]

      if (databaseMetaData == null) {
        val connector = createConnector(connectorId)

        databaseMetaData = connector.retrieveDatabase() as InternalDatabaseMetaData

        databaseMap[connectorId] = databaseMetaData.withFilteredTables(connectorId)
      }

      databaseMetaData
    } catch (e: SQLException) {
      throw GuttenBaseException("DatabaseMetaData:$connectorId", e)
    }
  }

  /**
   * Reset table data, i.e. reload data from the data base.
   */
  open fun refreshDatabase(connectorId: String) {
    databaseMap.remove(connectorId)
  }

  /**
   * Create connector object to given database
   */
  open fun createConnector(connectorId: String): Connector {
    val connectionInfo: ConnectorInfo = getConnectionInfo(connectorId)

    return connectionInfo.createConnector(this, connectorId)
  }

  /**
   * Get configuration for given source database
   */
  open fun getSourceDatabaseConfiguration(connectorId: String): SourceDatabaseConfiguration {
    val connectionInfo: ConnectorInfo = getConnectionInfo(connectorId)
    val databaseType: DatabaseType = connectionInfo.databaseType

    return if (connectionInfo is ExportSQLConnectorInfo) {
      DefaultSourceDatabaseConfiguration(this)
    } else {
      sourceDatabaseConfigurationMap[databaseType]
        ?: throw IllegalStateException("Unhandled source connector data base type: $databaseType")
    }
  }

  /**
   * Define configuration for given source database type when reading data.
   */
  open fun addSourceDatabaseConfiguration(
    databaseType: DatabaseType,
    sourceDatabaseConfiguration: SourceDatabaseConfiguration
  ): ConnectorRepository {
    sourceDatabaseConfigurationMap[databaseType] = sourceDatabaseConfiguration
    return this
  }

  /**
   * Define configuration for given target database type when reading data.
   */
  open fun addTargetDatabaseConfiguration(
    databaseType: DatabaseType,
    targetDatabaseConfiguration: TargetDatabaseConfiguration
  ): ConnectorRepository {
    targetDatabaseConfigurationMap[databaseType] = targetDatabaseConfiguration
    return this
  }

  /**
   * Get configuration for given target database
   */
  open fun getTargetDatabaseConfiguration(connectorId: String): TargetDatabaseConfiguration {
    val connectionInfo = getConnectionInfo(connectorId)
    val databaseType = connectionInfo.databaseType

    return if (connectionInfo is ExportSQLConnectorInfo) {
      DefaultTargetDatabaseConfiguration(this)
    } else {
      targetDatabaseConfigurationMap[databaseType]
        ?: throw IllegalStateException("Unhandled target connector data base type: $databaseType")
    }
  }

  private fun InternalDatabaseMetaData.withFilteredTables(connectorId: String): InternalDatabaseMetaData {
    val tableFilter = hint<RepositoryTableFilter>(connectorId)
    val columnFilter = hint<RepositoryColumnFilter>(connectorId)

    for (tableMetaData in tables) {
      if (tableFilter.accept(tableMetaData)) {
        for (columnMetaData in tableMetaData.columns) {
          if (!columnFilter.accept(columnMetaData)) {
            (tableMetaData as InternalTableMetaData).removeColumn(columnMetaData)
          }
        }
      } else {
        removeTable(tableMetaData)
      }
    }

    return this
  }

  private fun initDefaultConfiguration() {
    addSourceDatabaseConfiguration(GENERIC, GenericSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MOCK, GenericSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(IBMDB2, Db2SourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MSSQL, MsSqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MYSQL, MySqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MARIADB, MariaDbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(POSTGRESQL, PostgresqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(ORACLE, OracleSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(HSQLDB, HsqldbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(H2DB, H2DbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(DERBY, DerbySourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MS_ACCESS, MsAccessSourceDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(GENERIC, GenericTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MOCK, GenericTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(IBMDB2, Db2TargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MSSQL, MsSqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MYSQL, MySqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MARIADB, MariaDbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(ORACLE, OracleTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(POSTGRESQL, PostgresqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(HSQLDB, HsqldbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(H2DB, H2DbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(DERBY, DerbyTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MS_ACCESS, MsAccessTargetDatabaseConfiguration(this))
  }

  private fun initDefaultHints(connectorId: String) {
    addConnectorHint(connectorId, DefaultRepositoryTableFilterHint)
    addConnectorHint(connectorId, DefaultDatabaseTableFilterHint)
    addConnectorHint(connectorId, DefaultDatabaseColumnFilterHint)
    addConnectorHint(connectorId, object : DatabaseIndexFilterHint() {
      override val value: DatabaseIndexFilter
        get() = DefaultDatabaseIndexFilter()
    })
    addConnectorHint(connectorId, object : DatabaseForeignKeyFilterHint() {
      override val value: DatabaseForeignKeyFilter
        get() = DefaultDatabaseForeignKeyFilter()
    })
    addConnectorHint(connectorId, DefaultBatchInsertionConfigurationHint)
    addConnectorHint(connectorId, DefaultResultSetParametersHint)
    addConnectorHint(connectorId, DefaultNumberOfCheckedTableDataHint)
    addConnectorHint(connectorId, DefaultSplitColumnHint)
    addConnectorHint(connectorId, DefaultEntityTableCheckerHint)
    addConnectorHint(connectorId, DefaultColumnDataMapperProviderHint)
    addConnectorHint(connectorId, DefaultTableOrderHint())
    addConnectorHint(connectorId, DefaultColumnOrderHint)
    addConnectorHint(connectorId, DefaultAutoIncrementValueHint)
    addConnectorHint(connectorId, DefaultTableMapperHint)
    addConnectorHint(connectorId, DefaultColumnMapperHint)
    addConnectorHint(connectorId, DefaultPreparedStatementPlaceholderFactoryHint)
    addConnectorHint(connectorId, DefaultRepositoryColumnFilterHint)
    addConnectorHint(connectorId, DefaultTableCopyProgressIndicatorHint)
    addConnectorHint(connectorId, DefaultScriptExecutorProgressIndicatorHint)
    addConnectorHint(connectorId, DefaultRefreshTargetConnectionHint)
    addConnectorHint(connectorId, DefaultColumnTypeMapperHint)
    addConnectorHint(connectorId, DefaultSelectWhereClauseHint)
    addConnectorHint(connectorId, DefaultTableRowCountFilterHint)
    addConnectorHint(connectorId, DefaultTableRowDataFilterHint)
    addConnectorHint(connectorId, object : IndexMapperHint() {
      override val value: IndexMapper get() = IndexMapper { fixName(it.indexName) }
    })
    addConnectorHint(connectorId, object : ForeignKeyMapperHint() {
      override val value: ForeignKeyMapper get() = ForeignKeyMapper { fixName(it.foreignKeyName) }
    })
  }

  private fun fixName(name: String) = name.uppercase().replace('-', '_')
}

typealias JdbcDatabaseMetaData = java.sql.DatabaseMetaData

inline fun <reified T> ConnectorRepository.hint(connectorId: String): T =
  getConnectorHint(connectorId, T::class.java).value

