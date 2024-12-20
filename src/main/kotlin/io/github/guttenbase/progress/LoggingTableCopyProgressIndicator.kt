package io.github.guttenbase.progress

import io.github.guttenbase.utils.Util.ARROW
import io.github.guttenbase.utils.Util.formatTime
import org.slf4j.LoggerFactory

/**
 * &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class LoggingTableCopyProgressIndicator : TableCopyProgressIndicator {
  private val timingDelegate = TimingProgressIndicator

  override fun initializeIndicator() {
    timingDelegate.initializeIndicator()
  }

  override fun startProcess(numberOfTables: Int) {
    timingDelegate.startProcess(numberOfTables)
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {
    timingDelegate.startCopyTable(sourceTableName, rowCount, targetTableName)
    LOG.info(
      "Copying of " + timingDelegate.sourceTableName
          + " $ARROW "
          + timingDelegate.targetTableName
          + "("
          + timingDelegate.itemCounter
          + "/"
          + rowCount
          + ") started"
    )
  }

  override fun startExecution(action: String) {
    timingDelegate.startExecution(action)
  }

  override fun endExecution(totalCopiedRows: Int) {
    timingDelegate.endExecution(totalCopiedRows)
    val batchTime = formatTime(timingDelegate.elapsedExecutionTime)
    val tableTime = formatTime(timingDelegate.elapsedProcessTime)
    val totalTime = formatTime(timingDelegate.elapsedTotalTime)
    LOG.info(
      timingDelegate.sourceTableName + ":"
          + totalCopiedRows + "/" + timingDelegate.rowCount
          + " lines copied."
          + " Last batch took: " + batchTime
          + " Table time spent: " + tableTime
          + " Total time spent: " + totalTime
    )
  }

  override fun endProcess() {
    timingDelegate.endProcess()
    LOG.info(
      "Copying of " + timingDelegate.sourceTableName
          + " $ARROW "
          + timingDelegate.targetTableName
          + " took "
          + formatTime(timingDelegate.elapsedProcessTime)
    )
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
    LOG.info(
      "Copying of " + timingDelegate.numberOfTables + " tables took " + formatTime(timingDelegate.elapsedTotalTime)
    )
  }

  override fun updateTimers() {
    throw UnsupportedOperationException()
  }

  companion object {
    private val LOG = LoggerFactory.getLogger(LoggingTableCopyProgressIndicator::class.java)
  }
}
