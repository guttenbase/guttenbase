package io.github.guttenbase.progress

/**
 * Common interface.
 *
 * 2013-2034 tech@spree
 *
 * @author M. Dahm
 */
interface ProgressIndicator {
  fun initializeIndicator()
  fun startProcess(numberOfTables: Int)
  fun startExecution(action: String)
  fun endExecution(totalCopiedRows: Int)
  fun endProcess()
  fun updateTimers()
  fun finalizeIndicator()
  fun warn(text: String)
  fun info(text: String)
  fun debug(text: String)
}
