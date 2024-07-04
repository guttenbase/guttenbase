package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.progress.LoggingScriptExecutorProgressIndicator
import io.github.guttenbase.progress.ScriptExecutorProgressIndicator

/**
 * By default return fancy progress var implementation.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class LoggingScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
  override val value: ScriptExecutorProgressIndicator
    get() = LoggingScriptExecutorProgressIndicator()
}
