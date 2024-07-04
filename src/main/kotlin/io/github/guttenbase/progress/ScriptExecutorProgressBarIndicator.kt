package io.github.guttenbase.progress

import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.CLEAR_SCREEN
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.ERASE_RIGHT
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.PROGRESS_LOG
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.linesUp
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.progressBar
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.progressbar
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.status
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.stripNewlines
import org.apache.commons.io.ThreadUtils
import java.time.Duration

class ScriptExecutorProgressBarIndicator @JvmOverloads constructor(
  private val messageLength: Int = 20,
  private val progressBarLength: Int = 50
) : ScriptExecutorProgressIndicator {
  private var totalStatementCount = 0
  private var statementCount = 0
  private var action = ""
  private val initialProgressbar = progressBar(progressBarLength, 0)

  override fun initializeIndicator() {
    print(CLEAR_SCREEN)
  }

  override fun finalizeIndicator() {
  }

  override fun startProcess(numberOfTables: Int) {
    this.totalStatementCount = numberOfTables

    val overallProgress = status(messageLength, totalStatementCount, 0, "Statements", initialProgressbar)

    println(overallProgress)
  }

  override fun startExecution(action: String) {
    this.action = action
  }

  override fun endExecution(totalCopiedRows: Int) {
    this.statementCount += totalCopiedRows

    val progressBar = progressbar(progressBarLength, statementCount, totalStatementCount)
    val overallProgress = status(messageLength, totalStatementCount, statementCount, action, progressBar)

    print(linesUp(1) + ERASE_RIGHT)
    println(overallProgress)
  }

  override fun endProcess() {
  }

  override fun updateTimers() {
  }

  override fun warn(text: String) {
    if (PROGRESS_LOG.isWarnEnabled) {
      println(ERASE_RIGHT + "WARNING: " + stripNewlines(text))
      print(linesUp(1))
    }
  }

  override fun info(text: String) {
    if (PROGRESS_LOG.isInfoEnabled) {
      println(ERASE_RIGHT + "INFO: " + stripNewlines(text))
      print(linesUp(1))
    }
  }

  override fun debug(text: String) {
    if (PROGRESS_LOG.isDebugEnabled) {
      println(ERASE_RIGHT + "DEBUG: " + stripNewlines(text))
      print(linesUp(1))
    }
  }

  @Suppress("MemberVisibilityCanBePrivate")
  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      val indicator = ScriptExecutorProgressBarIndicator()
      indicator.initializeIndicator()
      val numberOfStatements = 15
      indicator.startProcess(numberOfStatements)

      for (tables in 1..numberOfStatements) {
        val action = when {
          tables < 5 -> "INSERT INTO FOOTABLE("
          tables < 10 -> "UPDATE BLA SET X = Y"
          else -> "ALTER TABLE FOO"
        }
        indicator.startExecution(action)

        ThreadUtils.sleep(Duration.ofMillis(50))
        indicator.endExecution(1)
      }

      indicator.endProcess()
      indicator.finalizeIndicator()
    }
  }
}