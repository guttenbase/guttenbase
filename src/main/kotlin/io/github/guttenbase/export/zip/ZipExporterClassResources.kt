package io.github.guttenbase.export.zip

import java.net.URL

/**
 * When exporting to e JAR/ZIP file we allow to add custom classes and resources to the resultiung JAR.
 *
 *
 * This allows to create a self-contained executable JAR that will startup with a Main class customizable by the framework user.
 *
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ZipExporterClassResources {
  /**
   * Startup class that will be written into the MANIFEST file.
   */
  val startupClass: Class<*>

  /**
   * List of classes that need to be added to the JAR. I.e. all resources found on the same originating resource (whether from file system
   * or JAR) will be added to the JAR, too. The list should contain the startup class as the first entry.
   */
  val classResources: List<Class<*>>

  /**
   * List of other resources to add to the dump, e.g. generated SQL scripts. The map key will be used as the name of the ZIP file entry. The
   * contents of the map value will be dumped into the zip entry.
   */
  val urlResources: Map<String, URL?>
}
