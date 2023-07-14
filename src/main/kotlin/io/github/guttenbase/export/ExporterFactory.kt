package io.github.guttenbase.export

/**
 * Create @see [Exporter] for dumping database using @see [ExportDumpConnector].
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
fun interface ExporterFactory {
  fun createExporter(): Exporter
}
