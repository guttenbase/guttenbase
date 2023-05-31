package io.github.guttenbase.repository.export

/**
 * Create @see [Exporter] for dumping database using @see [ExportDumpConnector].
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ExporterFactory {
  fun createExporter(): Exporter
}
