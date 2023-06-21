package io.github.guttenbase.configuration

import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.connector.impl.URLConnectorInfoImpl

class TestH2ConnectionInfo @JvmOverloads constructor(schema: String = "") : URLConnectorInfoImpl(
  "jdbc:h2:" + DB_DIRECTORY.absolutePath + "/h2_" + count++, "sa", "sa", "org.h2.Driver", schema, DatabaseType.H2DB
)