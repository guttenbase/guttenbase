package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.progress.ScriptExecutorProgressIndicator
import io.github.guttenbase.progress.SwingScriptExecutorProgressIndicator

/**
 * Use UI to show progress.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
open class SwingScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
 override val value: ScriptExecutorProgressIndicator
    get() = SwingScriptExecutorProgressIndicator()
}
