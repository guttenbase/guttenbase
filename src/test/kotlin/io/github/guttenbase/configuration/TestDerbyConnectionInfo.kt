package io.github.guttenbase.configuration

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.impl.URLConnectorInfoImpl

class TestDerbyConnectionInfo : URLConnectorInfoImpl(
  "jdbc:derby:" + DB_DIRECTORY + "/derby_" + count++ + ";create=true", "sa", "sa", "org.apache.derby.iapi.jdbc.AutoloadedDriver", "",
  DatabaseType.DERBY
)
