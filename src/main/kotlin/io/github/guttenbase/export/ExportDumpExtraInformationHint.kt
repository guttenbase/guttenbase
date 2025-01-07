package io.github.guttenbase.export


import io.github.guttenbase.hints.ConnectorHint

/**
 * When exporting to e JAR/ZIP file the user may add extra informations to the dumped data.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.export.zip.ZipExporter] to add custom informations to the dump
 * Hint is used by [io.github.guttenbase.export.gzip.PlainGzipExporter] to add custom informations to the dump
 *
 * @author M. Dahm
 */
abstract class ExportDumpExtraInformationHint : ConnectorHint<ExportDumpExtraInformation> {
  override val connectorHintType: Class<ExportDumpExtraInformation>
    get() = ExportDumpExtraInformation::class.java
}
