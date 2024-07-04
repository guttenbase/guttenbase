package io.github.guttenbase.progress

import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.CLEAR_SCREEN
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.EMPTY_PROGRESSBAR
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.ERASE_RIGHT
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.linesUp
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.progressbar
import io.github.guttenbase.progress.TableCopyProgressBarIndicator.Companion.status
import org.apache.commons.io.ThreadUtils
import java.time.Duration

class ScriptExecutorProgressBarIndicator : ScriptExecutorProgressIndicator {
  private var totalStatementCount = 0
  private var statementCount = 0
  private var action = ""

  override fun initializeIndicator() {
    print(CLEAR_SCREEN)
  }

  override fun finalizeIndicator() {
  }

  override fun startProcess(numberOfTables: Int) {
    this.totalStatementCount = numberOfTables

    val overallProgress = status(totalStatementCount, 0, "Statements", EMPTY_PROGRESSBAR)

    println(overallProgress)
  }

  override fun startExecution(action: String) {
    this.action = action
  }

  override fun endExecution(totalCopiedRows: Int) {
    this.statementCount += totalCopiedRows

    val progressBar = progressbar(statementCount, totalStatementCount)
    val overallProgress = status(totalStatementCount, statementCount, action, progressBar)

    print(linesUp(1) + ERASE_RIGHT)
    println(overallProgress)
  }

  override fun endProcess() {
  }

  override fun updateTimers() {
  }

  override fun warn(text: String) {
    println("Warning: $text")
    print(linesUp(1))
  }

  override fun info(text: String) {
    println("Info: $text")
    print(linesUp(1))
  }

  override fun debug(text: String) {
    println("Debug: $text")
    print(linesUp(1))
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