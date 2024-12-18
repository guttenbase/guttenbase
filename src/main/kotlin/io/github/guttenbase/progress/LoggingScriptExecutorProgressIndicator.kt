package io.github.guttenbase.progress

import org.slf4j.LoggerFactory

/**
 * &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
object LoggingScriptExecutorProgressIndicator : ScriptExecutorProgressIndicator {
  private val timingDelegate = TimingProgressIndicator

  override fun initializeIndicator() {
    timingDelegate.initializeIndicator()
  }

  override fun startProcess(numberOfTables: Int) {
    timingDelegate.startProcess(numberOfTables)
  }

  override fun startExecution(action: String) {
    timingDelegate.startExecution(action)
  }

  override fun endExecution(totalCopiedRows: Int) {
    timingDelegate.endExecution(totalCopiedRows)
  }

  override fun endProcess() {
    timingDelegate.endProcess()
  }

  override fun warn(text: String) {
    timingDelegate.warn(text)
    LOG.warn(text)
  }

  override fun info(text: String) {
    timingDelegate.info(text)
    LOG.info(text)
  }

  override fun debug(text: String) {
    timingDelegate.debug(text)
    LOG.debug(text)
  }

  override fun finalizeIndicator() {
    timingDelegate.finalizeIndicator()
  }

  override fun updateTimers() {
    throw UnsupportedOperationException()
  }

  @JvmStatic
  private val LOG = LoggerFactory.getLogger(LoggingScriptExecutorProgressIndicator::class.java)
}
