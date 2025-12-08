package io.github.guttenbase.meta

import java.util.*

/**
 * Extension for internal purposes.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
interface MetaData {
  /**
   * Synthetic unique identifier within the whole database
   */
  val syntheticId: UUID
}