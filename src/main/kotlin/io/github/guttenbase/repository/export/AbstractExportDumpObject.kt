package io.github.guttenbase.repository.export

import io.github.guttenbase.connector.GuttenBaseException
import org.apache.commons.io.IOUtils
import java.io.*

/**
 * Since CLOBs/BLOBs may be quite big, we do not load them into memory
 * completely, but read them in chunks and write the data to the output stream
 * in a loop.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
abstract class AbstractExportDumpObject @JvmOverloads constructor(@field:Transient private val inputStream: InputStream) :
  Externalizable {
  @Transient
  private lateinit var tempFile: File

  @Transient
  private lateinit var fileInputStream: FileInputStream

  /**
   * Read data in chunks and write it to the outputstream to avoid out of memory
   * errors.
   */
  @Throws(IOException::class)
  override fun writeExternal(output: ObjectOutput) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var n = inputStream.read(buffer)

    while (n > 0) {
      var buf = buffer

      if (n < DEFAULT_BUFFER_SIZE) {
        buf = ByteArray(n)
        System.arraycopy(buffer, 0, buf, 0, n)
      }
      output.writeObject(buf)
      output.flush()
      n = inputStream.read(buffer)
    }

    output.writeObject(null)
  }

  /**
   * Store read data in temporary file to avoid out of memory errors.
   */
  @Throws(IOException::class, ClassNotFoundException::class)
  override fun readExternal(input: ObjectInput) {
    if (!this::tempFile.isInitialized) {
      tempFile = File.createTempFile("GB-DUMP-", null)
      tempFile.deleteOnExit()
    }
    val fileOutputStream = FileOutputStream(tempFile)
    var buffer = input.readObject() as ByteArray?

    while (buffer != null) {
      fileOutputStream.write(buffer, 0, buffer.size)
      buffer = input.readObject() as ByteArray?
    }

    fileOutputStream.close()
  }

  fun length() = tempFile.length()

  fun getBytes(pos: Long, length: Int): ByteArray {
    return try {
      val inputStream = getBinaryStream(pos, length.toLong())
      val bytes = ByteArray(length)

      inputStream.read(bytes)
      inputStream.close()
      bytes
    } catch (e: IOException) {
      throw GuttenBaseException("getBytes", e)
    }
  }

  fun getBinaryStream() = getBinaryStream(0, length())

  fun getBinaryStream(pos: Long, @Suppress("unused") length: Long): InputStream {
    return try {
      fileInputStream = FileInputStream(tempFile)
      fileInputStream.skip(pos)
      fileInputStream
    } catch (e: IOException) {
      throw GuttenBaseException("getBinaryStream", e)
    }
  }

  fun free() {
    if (this::tempFile.isInitialized && tempFile.exists()) {
      tempFile.delete()
    }

    if (this::fileInputStream.isInitialized) {
      IOUtils.closeQuietly(fileInputStream)
    }
  }

  companion object {
    private const val serialVersionUID = 1L
    const val DEFAULT_BUFFER_SIZE = 1024 * 1024 * 10
  }
}
