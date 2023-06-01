package io.github.guttenbase.export.zip

/**
 * By default use the [ZipStartup] class as the main class of the JAR. Adds all GuttenBase and log4j classes to the JAR.
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class DefaultZipExporterClassResourcesHint : ZipExporterClassResourcesHint() {
  override val value: ZipExporterClassResources
    get() = DefaultZipExporterClassResources()
}
