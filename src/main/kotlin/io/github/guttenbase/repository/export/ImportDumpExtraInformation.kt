package io.github.guttenbase.repository.export

import java.io.Serializable

/**
 * Give the user a possibility to retrieve extra informations from the dumped data.
 *
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
interface ImportDumpExtraInformation {
  @Throws(Exception::class)
  fun processExtraInformation(extraInformation: Map<String?, Serializable?>?)
}
