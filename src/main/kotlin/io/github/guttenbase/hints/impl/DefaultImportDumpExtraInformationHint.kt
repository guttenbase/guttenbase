package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ImportDumpExtraInformation
import io.github.guttenbase.export.zip.ImportDumpExtraInformationHint


/**
 * By default do nothing.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
object DefaultImportDumpExtraInformationHint : ImportDumpExtraInformationHint() {
 override val value: ImportDumpExtraInformation
    get() = ImportDumpExtraInformation {  }
}
