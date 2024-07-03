package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.progress.LoggingScriptExecutorProgressIndicator
import io.github.guttenbase.progress.ScriptExecutorProgressIndicator


/**
 * By default return logging implementation.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
 override val value: ScriptExecutorProgressIndicator
    get() = LoggingScriptExecutorProgressIndicator()
}
