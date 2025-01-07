package io.github.guttenbase.export.zip


import io.github.guttenbase.export.ExporterFactory
import io.github.guttenbase.hints.ConnectorHint

/**
 * Create @see [io.github.guttenbase.export.Exporter] for dumping database using @see [io.github.guttenbase.export.ExportDumpConnector].
 *
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.export.ExportDumpConnector] to determine exporter implementation
 *
 * @author M. Dahm
 */
abstract class ExporterFactoryHint : ConnectorHint<ExporterFactory> {
  override val connectorHintType: Class<ExporterFactory>
    get() = ExporterFactory::class.java
}
