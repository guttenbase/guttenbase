package io.github.guttenbase.progress

import javax.swing.JDialog

/**
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
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
      } catch (_: InterruptedException) {
      }
      for (progressIndicator in progressIndicators) {
        progressIndicator.updateTimers()
      }
    }
  }
}
