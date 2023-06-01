package io.github.guttenbase.export.zip

import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import java.net.URL

/**
 * Default implementation.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DefaultZipExporterClassResources : ZipExporterClassResources {
  override val startupClass: Class<*>
    get() = ZipStartup::class.java

  override val classResources
    get() = listOf(startupClass, ZipExporter::class.java, Logger::class.java, IOUtils::class.java)

  override val urlResources: Map<String, URL?>
    get() = HashMap()
}