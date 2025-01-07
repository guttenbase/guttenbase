package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ImporterFactory
import io.github.guttenbase.export.zip.ImporterFactoryHint
import io.github.guttenbase.export.zip.ZipImporter


/**
 * Default implementation uses [ZipImporter].
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultImporterFactoryHint : ImporterFactoryHint() {
  override val value: ImporterFactory get() = ImporterFactory { ZipImporter() }
}
