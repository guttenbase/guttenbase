package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.utils.LoggingScriptExecutorProgressIndicator
import io.github.guttenbase.utils.ScriptExecutorProgressIndicator


/**
 * By default return logging implementation.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class DefaultScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
 override val value: ScriptExecutorProgressIndicator
    get() = LoggingScriptExecutorProgressIndicator()
}
