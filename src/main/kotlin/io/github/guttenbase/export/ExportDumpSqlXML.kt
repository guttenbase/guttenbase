package io.github.guttenbase.export

import io.github.guttenbase.connector.GuttenBaseException
import org.apache.commons.io.IOUtils
import java.io.*
import java.nio.charset.StandardCharsets
import java.sql.SQLXML
import javax.xml.transform.Result
import javax.xml.transform.Source

/**
 * Since XML data may be quite big. we do not load them into memory completely,
 * but read them in chunks and write the data to the output stream in a loop.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ExportDumpSqlXML(inputStream: InputStream) : AbstractExportDumpObject(inputStream), SQLXML {
  override fun getCharacterStream(): Reader = InputStreamReader(binaryStream)

  override fun setBinaryStream(): OutputStream {
    throw UnsupportedOperationException()
  }

  override fun setCharacterStream(): Writer {
    throw UnsupportedOperationException()
  }

  override fun getString(): String = try {
    IOUtils.toString(binaryStream, StandardCharsets.UTF_8)
  } catch (e: IOException) {
    throw GuttenBaseException("getString", e)
  }

  override fun setString(value: String) {
    throw UnsupportedOperationException()
  }

  override fun <T : Source> getSource(sourceClass: Class<T>): T {
    throw UnsupportedOperationException()
  }

  override fun <T : Result> setResult(resultClass: Class<T>): T {
    throw UnsupportedOperationException()
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
