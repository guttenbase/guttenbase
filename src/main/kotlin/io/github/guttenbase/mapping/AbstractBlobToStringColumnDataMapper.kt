package io.github.guttenbase.mapping

import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.sql.Blob
import java.sql.SQLException

/**
 * Map BLOB object to String using given charset. Use in conjunction with [io.github.guttenbase.defaults.impl.DefaultColumnDataMapperProvider]
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractBlobToStringColumnDataMapper
@JvmOverloads constructor(private val charset: Charset = StandardCharsets.UTF_8) : ColumnDataMapper {
  @Throws(SQLException::class)
  override fun map(mapping: ColumnDataMapping, value: Any?): Any? {
    val blob = value as Blob

    try {
      blob.binaryStream.use {
        InputStreamReader(it, charset).use { stream ->
          val available: Int = it.available()
          val bytes = CharArray(available)
          val read = stream.read(bytes)
          val availableAfterRead: Int = it.available()

          if (read < available && availableAfterRead > 0) {
            throw SQLException("Bytes read $read < available $available")
          }

          return String(bytes)
        }
      }
    } catch (e: IOException) {
      throw SQLException("getBinaryStream", e)
    } finally {
      blob.free()
    }
  }
}
