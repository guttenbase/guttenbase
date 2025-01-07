package io.github.guttenbase.export.zip

import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import java.net.URL

/**
 * Default implementation.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultZipExporterClassResources : ZipExporterClassResources {
  override val startupClass: Class<*>
    get() = ZipStartup::class.java

  override val classResources
    get() = listOf(startupClass, ZipExporter::class.java, Logger::class.java, IOUtils::class.java)

  override val urlResources: Map<String, URL?>
    get() = HashMap()
}