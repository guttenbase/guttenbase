package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.ScriptExecutorProgressIndicatorHint
import io.github.guttenbase.utils.ScriptExecutorProgressIndicator
import io.github.guttenbase.utils.SwingScriptExecutorProgressIndicator

/**
 * Use UI to show progress.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class SwingScriptExecutorProgressIndicatorHint : ScriptExecutorProgressIndicatorHint() {
 override val value: ScriptExecutorProgressIndicator
    get() = SwingScriptExecutorProgressIndicator()
}
