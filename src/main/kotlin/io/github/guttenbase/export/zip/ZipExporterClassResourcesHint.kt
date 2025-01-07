package io.github.guttenbase.export.zip

import io.github.guttenbase.hints.ConnectorHint


/**
 * When exporting to e JAR/ZIP file we allow to add custom classes and resources to the resulting JAR.
 *
 *
 * This allows to create a self-contained executable JAR that will startup with a Main class customizable by the framework user.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [ZipExporter] to add custom classes to the generated JAR and configure the META-INF/MANIFEST.MF Main-Class entry
 *
 * @author M. Dahm
 */
abstract class ZipExporterClassResourcesHint : ConnectorHint<ZipExporterClassResources> {
  override val connectorHintType: Class<ZipExporterClassResources>
    get() = ZipExporterClassResources::class.java
}
