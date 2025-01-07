package io.github.guttenbase.export

import java.io.Serializable

/**
 * Give the user a possibility to retrieve extra informations from the dumped data.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
fun interface ImportDumpExtraInformation {
  @Throws(Exception::class)
  fun processExtraInformation(extraInformation: Map<String, Serializable>)
}
