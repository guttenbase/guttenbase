package io.github.guttenbase.configuration

import io.github.guttenbase.meta.DatabaseType
import io.github.guttenbase.connector.impl.URLConnectorInfoImpl
import java.io.File

class TestHsqlConnectionInfo : URLConnectorInfoImpl("jdbc:hsqldb:" + DB_DIRECTORY + "/hsqldb" + count++, "sa", "", "org.hsqldb.jdbcDriver", "",
  DatabaseType.HSQLDB)

var count = 1
val DB_DIRECTORY = File("target/db")
