package io.github.guttenbase.configuration

import java.sql.*
import java.util.*

class MockDriver private constructor() : Driver {
  lateinit var info: MockConnectionInfo

  override fun connect(url: String, info: Properties) = this.info.connection

  override fun acceptsURL(url: String) = true

  override fun getPropertyInfo(url: String, info: Properties) = arrayOf<DriverPropertyInfo>()

  override fun getMajorVersion() = 0

  override fun getMinorVersion() = 0

  override fun jdbcCompliant() = true

  // JRE 1.7
  @Throws(SQLFeatureNotSupportedException::class)
  override fun getParentLogger() = null

  companion object {
    val INSTANCE = MockDriver()

    private var _registered = false

    init {
      load()
    }

    /**
     * INTERNAL
     */
    private fun load(): Driver {
      try {
        if (!_registered) {
          _registered = true
          DriverManager.registerDriver(INSTANCE)
        }
      } catch (e: SQLException) {
        e.printStackTrace()
      }

      return INSTANCE
    }
  }
}
