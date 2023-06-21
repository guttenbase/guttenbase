package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ImporterFactory
import io.github.guttenbase.export.zip.ImporterFactoryHint
import io.github.guttenbase.export.zip.ZipImporter


/**
 * Default implementation uses [ZipImporter].
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultImporterFactoryHint : ImporterFactoryHint() {
 override val value: ImporterFactory
    get() = ImporterFactory { ZipImporter() }
}
