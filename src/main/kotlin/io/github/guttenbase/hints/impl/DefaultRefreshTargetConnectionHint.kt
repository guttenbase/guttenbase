package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.RefreshTargetConnectionHint
import io.github.guttenbase.tools.RefreshTargetConnection


/**
 * By default, the connection is never flushed.
 *
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
object DefaultRefreshTargetConnectionHint : RefreshTargetConnectionHint() {
  override val value: RefreshTargetConnection
    get() = object : RefreshTargetConnection {}
}
