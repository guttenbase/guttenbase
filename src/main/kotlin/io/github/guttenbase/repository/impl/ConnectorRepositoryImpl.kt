package io.github.guttenbase.repository.impl

import io.github.guttenbase.configuration.*
import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.ConnectorInfo
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.DatabaseType.*
import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.defaults.impl.DefaultColumnMapper
import io.github.guttenbase.export.ExportDumpDatabaseConfiguration
import io.github.guttenbase.export.ImportDumpDatabaseConfiguration
import io.github.guttenbase.export.zip.DefaultZipExporterClassResourcesHint
import io.github.guttenbase.hints.CaseConversionMode
import io.github.guttenbase.hints.ConnectorHint
import io.github.guttenbase.hints.impl.*
import io.github.guttenbase.mapping.ColumnMapper
import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.InternalDatabaseMetaData
import io.github.guttenbase.meta.InternalTableMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.repository.RepositoryColumnFilter
import io.github.guttenbase.repository.RepositoryTableFilter
import java.sql.SQLException
import java.util.*

/**
 * The main repository containing all configured connectors.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 * Hint is used by [io.github.guttenbase.hints.RepositoryTableFilterHint] when returning table metadata
 */
open class ConnectorRepositoryImpl : ConnectorRepository {
  private val connectionInfoMap = TreeMap<String, ConnectorInfo>()
  private val sourceDatabaseConfigurationMap = HashMap<DatabaseType, SourceDatabaseConfiguration>()
  private val targetDatabaseConfigurationMap = HashMap<DatabaseType, TargetDatabaseConfiguration>()

  /**
   * Cache metadata since some databases are very slow on retrieving it.
   */
  private val databaseMetaDataMap = HashMap<String, InternalDatabaseMetaData>()
  private val connectionHintMap = HashMap<String, MutableMap<Class<*>, ConnectorHint<*>>>()

  override val connectorIds: List<String> get() = ArrayList(connectionInfoMap.keys)

  init {
    initDefaultConfiguration()
  }

  /**
   * {@inheritDoc}
   */
  override fun addConnectionInfo(connectorId: String, connectionInfo: ConnectorInfo) {
    connectionInfoMap[connectorId] = connectionInfo
    initDefaultHints(connectorId, connectionInfo)
  }

  /**
   * {@inheritDoc}
   */
  override fun removeConnectionInfo(connectorId: String) {
    connectionInfoMap.remove(connectorId)
    connectionHintMap.remove(connectorId)
    databaseMetaDataMap.remove(connectorId)
  }

  /**
   * {@inheritDoc}
   */
  override fun <T : Any> addConnectorHint(connectorId: String, hint: ConnectorHint<T>) {
    // Check connector if is configured
    getConnectionInfo(connectorId)
    val hintMap = connectionHintMap.getOrPut(connectorId) { HashMap() }
    hintMap[hint.connectorHintType] = hint

    refreshDatabaseMetaData(connectorId)
  }

  /**
   * {@inheritDoc}
   */
  override fun <T : Any> removeConnectorHint(connectorId: String, connectionInfoHintType: Class<T>) {
    val hintMap = connectionHintMap[connectorId]
    hintMap?.remove(connectionInfoHintType)

    refreshDatabaseMetaData(connectorId)
  }

  /**
   * {@inheritDoc}
   */
  @Suppress("UNCHECKED_CAST")
  override fun <T : Any> getConnectorHint(connectorId: String, connectorHintType: Class<T>): ConnectorHint<T> {
    val hintMap = connectionHintMap[connectorId]
      ?: throw IllegalStateException("No hints defined for $connectorId")
    return hintMap[connectorHintType] as ConnectorHint<T>
  }

  /**
   * {@inheritDoc}
   */
  override fun getConnectionInfo(connectorId: String) =
    connectionInfoMap[connectorId] ?: throw IllegalStateException("Connector not configured: $connectorId")

  /**
   * {@inheritDoc}
   */
  override fun getDatabaseMetaData(connectorId: String): DatabaseMetaData {
    return try {
      var databaseMetaData: InternalDatabaseMetaData? = databaseMetaDataMap[connectorId]

      if (databaseMetaData == null) {
        val connector = createConnector(connectorId)

        databaseMetaData = connector.retrieveDatabaseMetaData() as InternalDatabaseMetaData

        databaseMetaDataMap[connectorId] = databaseMetaData.withFilteredTables(connectorId)
      }

      databaseMetaData
    } catch (e: SQLException) {
      throw GuttenBaseException("getDatabaseMetaData", e)
    }
  }

  /**
   * {@inheritDoc}
   */
  override fun refreshDatabaseMetaData(connectorId: String) {
    databaseMetaDataMap.remove(connectorId)
  }

  /**
   * {@inheritDoc}
   */
  override fun createConnector(connectorId: String): Connector {
    val connectionInfo: ConnectorInfo = getConnectionInfo(connectorId)

    return connectionInfo.createConnector(this, connectorId)
  }

  /**
   * {@inheritDoc}
   */
  override fun getSourceDatabaseConfiguration(connectorId: String): SourceDatabaseConfiguration {
    val connectionInfo: ConnectorInfo = getConnectionInfo(connectorId)
    val databaseType: DatabaseType = connectionInfo.databaseType

    return sourceDatabaseConfigurationMap[databaseType]
      ?: throw IllegalStateException("Unhandled source connector data base type: $databaseType")
  }

  /**
   * {@inheritDoc}
   */
  override fun addSourceDatabaseConfiguration(
    databaseType: DatabaseType,
    sourceDatabaseConfiguration: SourceDatabaseConfiguration
  ) {
    sourceDatabaseConfigurationMap[databaseType] = sourceDatabaseConfiguration
  }

  /**
   * {@inheritDoc}
   */
  override fun addTargetDatabaseConfiguration(
    databaseType: DatabaseType,
    targetDatabaseConfiguration: TargetDatabaseConfiguration
  ) {
    targetDatabaseConfigurationMap[databaseType] = targetDatabaseConfiguration
  }

  /**
   * {@inheritDoc}
   */
  override fun getTargetDatabaseConfiguration(connectorId: String): TargetDatabaseConfiguration {
    val connectionInfo: ConnectorInfo = getConnectionInfo(connectorId)
    val databaseType: DatabaseType = connectionInfo.databaseType

    return targetDatabaseConfigurationMap[databaseType]
      ?: throw IllegalStateException("Unhandled target connector data base type: $databaseType")
  }

  private fun InternalDatabaseMetaData.withFilteredTables(connectorId: String): InternalDatabaseMetaData {
    val tableFilter: RepositoryTableFilter = getConnectorHint(connectorId, RepositoryTableFilter::class.java).value
    val columnFilter: RepositoryColumnFilter = getConnectorHint(connectorId, RepositoryColumnFilter::class.java).value

    for (tableMetaData in tableMetaData) {
      if (tableFilter.accept(tableMetaData)) {
        for (columnMetaData in tableMetaData.columnMetaData) {
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
    addSourceDatabaseConfiguration(DB2, Db2SourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MSSQL, MsSqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MYSQL, MySqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MARIADB, MariaDbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(POSTGRESQL, PostgresqlSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(ORACLE, OracleSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(EXPORT_DUMP, ImportDumpDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(IMPORT_DUMP, ImportDumpDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(HSQLDB, HsqldbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(H2DB, H2DbSourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(DERBY, DerbySourceDatabaseConfiguration(this))
    addSourceDatabaseConfiguration(MS_ACCESS, MsAccessSourceDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(GENERIC, GenericTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MOCK, GenericTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(DB2, Db2TargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MSSQL, MsSqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MYSQL, MySqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MARIADB, MariaDbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(ORACLE, OracleTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(POSTGRESQL, PostgresqlTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(EXPORT_DUMP, ExportDumpDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(IMPORT_DUMP, ExportDumpDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(HSQLDB, HsqldbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(H2DB, H2DbTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(DERBY, DerbyTargetDatabaseConfiguration(this))
    addTargetDatabaseConfiguration(MS_ACCESS, MsAccessTargetDatabaseConfiguration(this))
  }

  private fun initDefaultHints(connectorId: String, connectorInfo: ConnectorInfo) {
    addConnectorHint(connectorId, DefaultRepositoryTableFilterHint())
    addConnectorHint(connectorId, DefaultDatabaseTableFilterHint())
    addConnectorHint(connectorId, DefaultDatabaseColumnFilterHint())
    addConnectorHint(connectorId, DefaultNumberOfRowsPerBatchHint())
    addConnectorHint(connectorId, DefaultResultSetParametersHint())
    addConnectorHint(connectorId, DefaultNumberOfCheckedTableDataHint())
    addConnectorHint(connectorId, DefaultMaxNumberOfDataItemsHint())
    addConnectorHint(connectorId, DefaultSplitColumnHint())
    addConnectorHint(connectorId, DefaultColumnTypeResolverListHint())
    addConnectorHint(connectorId, DefaultEntityTableCheckerHint())
    addConnectorHint(connectorId, DefaultExporterFactoryHint())
    addConnectorHint(connectorId, DefaultImporterFactoryHint())
    addConnectorHint(connectorId, DefaultZipExporterClassResourcesHint())
    addConnectorHint(connectorId, DefaultColumnDataMapperProviderHint())
    addConnectorHint(connectorId, DefaultTableOrderHint())
    addConnectorHint(connectorId, DefaultColumnOrderHint())
    addConnectorHint(connectorId, DefaultTableMapperHint())
    addConnectorHint(connectorId, DefaultColumnMapperHint(createColumnMapperHint(connectorInfo)))
    addConnectorHint(connectorId, DefaultRepositoryColumnFilterHint())
    addConnectorHint(connectorId, DefaultExportDumpExtraInformationHint())
    addConnectorHint(connectorId, DefaultImportDumpExtraInformationHint())
    addConnectorHint(connectorId, DefaultTableCopyProgressIndicatorHint())
    addConnectorHint(connectorId, DefaultScriptExecutorProgressIndicatorHint())
    addConnectorHint(connectorId, DefaultRefreshTargetConnectionHint())
    addConnectorHint(connectorId, DefaultColumnTypeMapperHint())
    addConnectorHint(connectorId, DefaultSelectWhereClauseHint())
    addConnectorHint(connectorId, DefaultTableRowCountFilterHint())
    addConnectorHint(connectorId, DefaultTableRowDataFilterHint())
  }

  private fun createColumnMapperHint(connectorInfo: ConnectorInfo): ColumnMapper {
    return when (connectorInfo.databaseType) {
      MYSQL, MARIADB -> DefaultColumnMapper(CaseConversionMode.NONE, "`")
      POSTGRESQL -> DefaultColumnMapper(CaseConversionMode.NONE, "\"")
      else -> DefaultColumnMapper()
    }
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
