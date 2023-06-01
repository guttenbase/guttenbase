package io.github.guttenbase.export

/**
 * Create @see [Importer] for reading dumped database using @see [ImportDumpConnector].
 *
 *
 *  2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface ImporterFactory {
  fun createImporter(): Importer
}
