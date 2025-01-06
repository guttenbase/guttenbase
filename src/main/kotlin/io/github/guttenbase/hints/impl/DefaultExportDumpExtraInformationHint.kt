package io.github.guttenbase.hints.impl

import io.github.guttenbase.export.ExportDumpExtraInformation
import io.github.guttenbase.export.ExportDumpExtraInformationHint

/**
 * By default do nothing.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultExportDumpExtraInformationHint : ExportDumpExtraInformationHint() {
 override val value: ExportDumpExtraInformation
    get() = ExportDumpExtraInformation {  _,_,_ -> HashMap() }
}
