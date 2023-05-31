package io.github.guttenbase.utils

import javax.swing.JDialog

class TimerDaemonThread(private val dialog: JDialog, vararg progressIndicators: ProgressIndicator) : Thread("GB-Timer-Daemon") {
  private val progressIndicators = listOf(*progressIndicators)
  var isActive = true

  init {
    isDaemon = true
  }

  override fun run() {
    dialog.isVisible = true

    while (isActive && dialog.isVisible) {
      try {
        sleep(800L)
      } catch (ignored: InterruptedException) {
      }
      for (progressIndicator in progressIndicators) {
        progressIndicator.updateTimers()
      }
    }
  }
}
