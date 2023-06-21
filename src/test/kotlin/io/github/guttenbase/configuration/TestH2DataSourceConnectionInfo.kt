package io.github.guttenbase.configuration

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.impl.DataSourceConnectorInfo
import org.h2.jdbcx.JdbcDataSource

class TestH2DataSourceConnectionInfo : DataSourceConnectorInfo(
  createDataSource(), "", "", "", DatabaseType.H2DB
) {
  companion object {
    private fun createDataSource() = JdbcDataSource().apply {
      setURL("jdbc:h2:${DB_DIRECTORY.absolutePath}/h2ds")
      user = "sa"
      password = "sa"
    }
  }
}
