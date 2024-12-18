package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.progress.ScriptExecutorProgressBarIndicator
import io.github.guttenbase.progress.ScriptExecutorProgressIndicator


/**
 * By default return fancy progress var implementation.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object DefaultScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
  override val value: ScriptExecutorProgressIndicator
    get() = ScriptExecutorProgressBarIndicator()
}
