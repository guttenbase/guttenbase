package io.github.guttenbase.progress

import io.github.guttenbase.utils.Util.formatTime
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.JDialog

/**
 * Swing UI for table copy
 *
 *
 *  &copy; 2013-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class SwingTableCopyProgressIndicator : TableCopyProgressIndicator {
  private val panel = TableCopyProgressIndicatorPanel()
  private val dialog = JDialog()
  private val timingDelegate = TimingProgressIndicator()
  private val text = StringBuilder()
  private var timerDaemonThread: TimerDaemonThread? = null

  init {
    dialog.isModal = true
    dialog.title = "Copying tables..."
    dialog.defaultCloseOperation = JDialog.DISPOSE_ON_CLOSE
    dialog.addWindowListener(object : WindowAdapter() {
      override fun windowClosed(e: WindowEvent) {
        if (dialog.isVisible && timerDaemonThread != null && timerDaemonThread!!.isActive) {
          finalizeIndicator()
        }
      }
    })
    val size = Dimension(800, 400)
    dialog.contentPane.layout = BorderLayout()
    dialog.contentPane.add(panel, BorderLayout.CENTER)
    dialog.size = size
    dialog.minimumSize = size
    panel.preferredSize = size
  }

  override fun initializeIndicator() {
    timingDelegate.initializeIndicator()
    panel.totalTime.text = ""
    panel.tableTime.text = ""
    panel.sourceTable.text = ""
    panel.targetTable.text = ""
    timerDaemonThread = TimerDaemonThread(dialog, timingDelegate, this)
    timerDaemonThread!!.start()
  }

  override fun startProcess(numberOfTables: Int) {
    timingDelegate.startProcess(numberOfTables)
    panel.totalProgress.value = 0
    panel.totalProgress.minimum = 0
    panel.totalProgress.maximum = numberOfTables
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {
    timingDelegate.startCopyTable(sourceTableName, rowCount, targetTableName)
    panel.tableProgress.minimum = 0
    panel.tableProgress.maximum = rowCount
    panel.tableProgress.value = 0
    panel.sourceTable.text = sourceTableName
    panel.targetTable.text = targetTableName
  }

  override fun startExecution(action: String) {
    timingDelegate.startExecution(action)
  }

  override fun endExecution(totalCopiedRows: Int) {
    timingDelegate.endExecution(totalCopiedRows)
    panel.tableProgress.value = totalCopiedRows
    updateTimers()
  }

  override fun endProcess() {
    timingDelegate.endProcess()
    panel.tableProgress.value = timingDelegate.rowCount
    panel.totalProgress.value = timingDelegate.itemCounter
  }

  override fun warn(text: String) {
    timingDelegate.warn(text)
    this.text.append("WARNING: ").append(text).append("\n")
    updateMessages()
  }

  override fun info(text: String) {
    timingDelegate.info(text)
    this.text.append("Info: ").append(text).append("\n")
    updateMessages()
  }

  override fun debug(text: String) {
    timingDelegate.debug(text)
    this.text.append("Debug: ").append(text).append("\n")
    updateMessages()
  }

  override fun finalizeIndicator() {
    timingDelegate.finalizeIndicator()
    timerDaemonThread?.isActive = false
    dialog.isVisible = false
    dialog.dispose()
    timerDaemonThread = null
  }

  override fun updateTimers() {
    panel.totalTime.text = formatTime(timingDelegate.elapsedTotalTime)
    panel.tableTime.text = formatTime(timingDelegate.elapsedProcessTime)
  }

  private fun updateMessages() {
    panel.messages.text = text.toString()
  }
}
