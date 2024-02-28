package io.github.guttenbase.connector.impl

import io.github.guttenbase.connector.Connector
import io.github.guttenbase.connector.DatabaseType
import io.github.guttenbase.repository.ConnectorRepository
import java.io.File
import java.io.InputStream
import java.util.Properties

/**
 * Read properties from file
 */
class PropertiesURLConnectorInfo(stream: InputStream) : URLConnectorInfo {
  constructor(file: File) : this(file.inputStream())

  constructor(file: String) : this(File(file))

  private val properties = Properties()

  init {
    stream.use { properties.load(stream) }
  }

  override val url: String get() = getProperty(URL)
  override val driver: String get() = getProperty(DRIVER)
  override val user: String get() = getProperty(USERNAME)
  override val password: String get() = getProperty(PASSWORD)
  override val schema: String get() = getProperty(SCHEMA)
  override val databaseType: DatabaseType get() = DatabaseType.valueOf(getProperty(TYPE).uppercase())

  private fun getProperty(name: String) =
    properties.getProperty(name) ?: throw IllegalStateException("$name property not found")

  /**
   * {@inheritDoc}
   */
  override fun createConnector(connectorRepository: ConnectorRepository, connectorId: String): Connector =
    URLConnector(connectorRepository, connectorId, this)
}

const val URL = "db.url"
const val DRIVER = "db.driver"
const val USERNAME = "db.username"
const val PASSWORD = "db.password"
const val TYPE = "db.type"
const val SCHEMA = "db.schema"