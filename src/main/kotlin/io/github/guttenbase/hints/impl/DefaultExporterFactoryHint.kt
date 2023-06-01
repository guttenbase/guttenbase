package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ExporterFactory
import io.github.guttenbase.export.zip.ExporterFactoryHint
import io.github.guttenbase.export.zip.ZipExporter

/**
 * Default implementation uses [ZipExporter].
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class DefaultExporterFactoryHint : ExporterFactoryHint() {
 override val value: ExporterFactory
    get() = ExporterFactory { ZipExporter() }
}
