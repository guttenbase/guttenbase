package io.github.guttenbase.export


import io.github.guttenbase.hints.ConnectorHint

/**
 * When exporting to e JAR/ZIP file we give the user a possibility to add extra informations to the dumped data.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [io.github.guttenbase.export.zip.ZipExporter] to add custom informations to the dump
 * Hint is used by [io.github.guttenbase.export.plain.PlainGzipExporter] to add custom informations to the dump
 *
 * @author M. Dahm
 */
abstract class ExportDumpExtraInformationHint : ConnectorHint<ExportDumpExtraInformation> {
  override val connectorHintType: Class<ExportDumpExtraInformation>
    get() = ExportDumpExtraInformation::class.java
}
