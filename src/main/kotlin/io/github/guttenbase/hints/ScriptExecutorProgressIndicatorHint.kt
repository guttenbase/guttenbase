package io.github.guttenbase.hints

import io.github.guttenbase.utils.ScriptExecutorProgressIndicator

/**
 * Select implementation of progress indicator. May be simple logger or fancy UI.
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * Hint is used by [ScriptExecutorTool]
 *
 * @author M. Dahm
 */
abstract class ScriptExecutorProgressIndicatorHint : ConnectorHint<ScriptExecutorProgressIndicator> {
  override val connectorHintType: Class<ScriptExecutorProgressIndicator>
    get() = ScriptExecutorProgressIndicator::class.java
}
