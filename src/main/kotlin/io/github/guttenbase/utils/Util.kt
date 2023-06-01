@file:Suppress("MemberVisibilityCanBePrivate")

package io.github.guttenbase.utils

import io.github.guttenbase.connector.GuttenBaseException
import org.slf4j.LoggerFactory
import java.io.*
import java.net.URL
import java.net.URLClassLoader
import java.util.*
import kotlin.properties.ReadOnlyProperty

/**
 * Collection of utility methods.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
object Util {
  @JvmStatic
  private val LOG = LoggerFactory.getLogger(Util::class.java)

  val ByteArrayClass: Class<*> = ByteArray::class.java

  const val DEFAULT_BUFFER_SIZE = 1024 * 4

  internal fun <T : Any> immutable(values: Collection<T>) =
    ReadOnlyProperty<Any, List<T>> { _, _ -> ArrayList(values) }

  /**
   * Read all non-empty lines for File and remove and trim them.
   *
   * @param resourceName Text file in CLASSPATH
   * @return array of strings
   */
  fun readLinesFromFile(resourceName: String, encoding: String): List<String> {
    val stream = getResourceAsStream(resourceName)

    return if (stream != null) {
      readLinesFromStream(stream, encoding)
    } else {
      LOG.warn("$resourceName not found")
      ArrayList()
    }
  }

  fun getResourceAsStream(resource: String): InputStream? {
    val stripped = if (resource.startsWith("/")) resource.substring(1) else resource
    val classLoader = Thread.currentThread().contextClassLoader
    var url = getResourceFromClassloader(resource, stripped, classLoader)

    if (url == null) {
      url = getResourceFromClassloader(resource, stripped, Util::class.java.classLoader)
    }

    if (url == null) {
      LOG.debug("Trying getResource")
      url = Util::class.java.getResource(resource)

      if (url == null) {
        url = Util::class.java.getResource(stripped)
      }
    }

    if (url != null) {
      try {
        return url.openStream()
      } catch (e: IOException) {
        LOG.warn("Can't open stream on $url")
      }
    }

    return null
  }

  private fun getResourceFromClassloader(resource: String, stripped: String, classLoader: ClassLoader): URL? {
    LOG.debug("Trying class loader {}", classLoader)

    var url: URL? = null

    if (classLoader is URLClassLoader) {
      LOG.debug("Trying as UCL class loader")
      val ucl: URLClassLoader = classLoader
      url = ucl.findResource(resource)

      if (url == null) {
        url = ucl.findResource(stripped)
      }
    }

    if (url == null) {
      url = classLoader.getResource(resource)
    }

    if (url == null) {
      url = classLoader.getResource(stripped)
    }

    return url
  }

  /**
   * Read all non-empty lines and remove and trim them.
   *
   * @param inputStream UTF8-encoded stream to read data from
   * @return list of strings
   */
  fun readLinesFromStream(inputStream: InputStream, encoding: String): List<String> {
    val result = ArrayList<String>()

    try {
      val reader = LineNumberReader(InputStreamReader(inputStream, encoding))
      var line: String

      while (reader.readLine().also { line = it } != null) {
        line = trim(line)
        if ("" != line) {
          result.add(line)
        }
      }
      inputStream.close()
    } catch (e: Exception) {
      LOG.error("Reading from inputstream", e)
    }

    return result
  }

  fun trim(src: String?) = src?.trim { it <= ' ' } ?: ""

  /**
   * Create deep copy of object.
   */
  fun <T : Any> copyObject(clazz: Class<T>, sourceObject: T): T {
    return try {
      val byteArray = toByteArray(sourceObject)
      fromByteArray(clazz, byteArray)
    } catch (e: Exception) {
      throw IllegalStateException("Can not copy ", e)
    }
  }

  /**
   * Serialize into byte array
   */
  @Throws(IOException::class)
  fun toByteArray(sourceObject: Any): ByteArray {
    val outStream = ByteArrayOutputStream()
    val out: ObjectOutput = ObjectOutputStream(outStream)

    return out.use {
      it.writeObject(sourceObject)
      outStream.toByteArray()
    }
  }

  /**
   * Deserialize from byte array
   *
   * @throws Exception
   */
  @Throws(Exception::class)
  fun <T : Any> fromByteArray(clazz: Class<T>, byteArray: ByteArray): T {
    val bis = ByteArrayInputStream(byteArray)
    val objectInputStream = ObjectInputStream(bis)

    return objectInputStream.use { clazz.cast(it.readObject()) }
  }

  /**
   * Deserialize from input stream
   *
   * @throws Exception
   */
  @Throws(Exception::class)
  fun <T> fromInputStream(clazz: Class<T>, inputStream: InputStream?): T {
    val objectInputStream = ObjectInputStream(inputStream)
    return clazz.cast(objectInputStream.readObject())
  }

  @Throws(IOException::class)
  fun copy(input: InputStream, output: OutputStream) {
    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
    var n: Int

    while (input.read(buffer).also { n = it } > 0) {
      output.write(buffer, 0, n)
    }
  }

  fun formatTime(millis: Long): String {
    var seconds = millis / 1000
    var minutes = seconds / 60
    val hours = minutes / 60
    minutes = minutes % 60
    seconds = seconds % 60
    return fillup(hours) + ":" + fillup(minutes) + ":" + fillup(seconds)
  }

  fun deleteDirectory(directory: File) {
    if (directory.exists() && directory.isDirectory) {
      val files = directory.list()!!

      for (fileName in files) {
        val file = File(directory, fileName)
        deleteDirectory(file)
      }
    }

    directory.delete()
  }

  private fun fillup(time: Long): String {
    return if (time > 9) time.toString() else "0$time"
  }

  val isWindows: Boolean
    get() {
      val os = System.getProperty("os.name").lowercase(Locale.getDefault())
      return os.contains("win")
    }

  /**
   * @return uppercased list of columns in SELECT statement
   */
  fun parseSelectedColumns(sql: String): List<String> {
    val result = ArrayList<String>()
    val stringTokenizer = StringTokenizer(sql, " ,\n\r\t")

    if (!"SELECT".equals(stringTokenizer.nextToken(), ignoreCase = true)) {
      throw GuttenBaseException("Cannot parse statement: No SELECT clause $sql")
    }

    var column: String = stringTokenizer.nextToken()

    while (stringTokenizer.hasMoreTokens()) {
      if ("FROM".equals(column, ignoreCase = true)) {
        return result
      } else {
        result.add(column.uppercase(Locale.getDefault()))
      }

      column = stringTokenizer.nextToken()
    }

    throw GuttenBaseException("Cannot parse statement missing FROM clause: $sql")
  }
}
