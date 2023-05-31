package io.github.guttenbase.repository.export

/**
 * Create @see [Importer] for reading dumped database using @see [ImportDumpConnector].
 *
 *
 *  2012-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
interface ImporterFactory {
  fun createImporter(): Importer?
}
