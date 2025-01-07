package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ExporterFactory
import io.github.guttenbase.export.zip.ExporterFactoryHint
import io.github.guttenbase.export.zip.ZipExporter

/**
 * Default implementation uses [ZipExporter].
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultExporterFactoryHint : ExporterFactoryHint() {
  override val value: ExporterFactory get() = ExporterFactory { ZipExporter() }
}
