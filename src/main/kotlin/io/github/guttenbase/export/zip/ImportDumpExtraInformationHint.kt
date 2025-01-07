package io.github.guttenbase.export.zip

import io.github.guttenbase.export.ImportDumpExtraInformation
import io.github.guttenbase.hints.ConnectorHint

/**
 * When exporting to JAR/ZIP file we give the user a possibility to retrieve extra informations from the dumped data.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * Hint is used by [ZipImporter] to add custom informations to the dump
 * Hint is used by [io.github.guttenbase.export.gzip.PlainGzipImporter] to add custom informations to the dump
 *
 * @author M. Dahm
 */
abstract class ImportDumpExtraInformationHint : ConnectorHint<ImportDumpExtraInformation> {
  override val connectorHintType: Class<ImportDumpExtraInformation>
    get() = ImportDumpExtraInformation::class.java
}
