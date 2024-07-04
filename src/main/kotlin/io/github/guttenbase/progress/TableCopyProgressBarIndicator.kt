package io.github.guttenbase.progress

import io.github.guttenbase.utils.Util.abbreviate
import org.apache.commons.io.ThreadUtils
import java.time.Duration

class TableCopyProgressBarIndicator : TableCopyProgressIndicator {
  private var totalTableCount = 0
  private var tableCount = 0
  private var totalRowCount = 0
  private var rowCount = 0
  private var tableName = ""

  override fun initializeIndicator() {
    print(CLEAR_SCREEN)
  }

  override fun finalizeIndicator() {
  }

  override fun startProcess(numberOfTables: Int) {
    this.totalTableCount = numberOfTables

    val overallProgress = status(totalTableCount, 0, "Tables", EMPTY_PROGRESSBAR)
    val currentProgress = status(0, 0, "Rows", EMPTY_PROGRESSBAR)

    println(overallProgress)
    println(currentProgress)
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {
    this.tableName = sourceTableName
    this.totalRowCount = rowCount
    this.rowCount = 0

    val progressBar = progressbar(tableCount, totalTableCount)
    val overallProgress = status(totalTableCount, ++tableCount, "Tables", progressBar)
    val currentProgress = status(totalRowCount, this.rowCount, tableName, EMPTY_PROGRESSBAR)

    print(linesUp(2) + ERASE_RIGHT)
    println(overallProgress)
    println(currentProgress)
  }

  override fun startExecution(action: String) {
  }

  override fun endExecution(totalCopiedRows: Int) {
    this.rowCount = totalCopiedRows

    val progressBar = progressbar(rowCount, totalRowCount)
    val currentProgress = status(totalRowCount, this.rowCount, tableName, progressBar)

    print(linesUp(1) + ERASE_RIGHT)
    println(currentProgress)
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
    // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
    const val CHAR_ESCAPE = ''
    const val CHAR_INCOMPLETE = '░' // U+2591 Unicode Character
    const val CHAR_COMPLETED = '█'  // U+2588 Unicode Character
    const val ESCAPE_START = "$CHAR_ESCAPE["
    const val ERASE_RIGHT = "${ESCAPE_START}0K"
    const val ERASE_SCREEN = "${ESCAPE_START}2J"
    const val HOME = "${ESCAPE_START}H"
    const val CLEAR_SCREEN = "${HOME}$ERASE_SCREEN"

    private const val PROGRESSBAR_SIZE = 50
    internal val EMPTY_PROGRESSBAR = progressBar(PROGRESSBAR_SIZE, 0)

    fun linesUp(n: Int) = "${ESCAPE_START}${n}F"

    internal fun status(totalCount: Int, count: Int, text: String, progressBar: String): String {
      val digits = totalCount.toString().length
      val digitFormat = "%0${digits}d"

      return "%-15s %50s ($digitFormat/$digitFormat)".format(text.abbreviate(15), progressBar, count, totalCount)
    }

    private fun progressBar(incomplete: Int, complete: Int) =
      "$CHAR_COMPLETED".repeat(complete) + "$CHAR_INCOMPLETE".repeat(incomplete)

    internal fun progressbar(count: Int, totalCount: Int): String {
      val percentage = count / totalCount.toDouble()
      val completed = (percentage * PROGRESSBAR_SIZE).toInt()
      val incomplete = PROGRESSBAR_SIZE - completed
      val progressBar = progressBar(incomplete, completed)

      return progressBar
    }

    @JvmStatic
    fun main(args: Array<String>) {
      val indicator = TableCopyProgressBarIndicator()
      indicator.initializeIndicator()
      val numberOfTables = 15
      indicator.startProcess(numberOfTables)
      val totalRows = 250000
      val step = 2500

      for (tables in 1..numberOfTables) {
        indicator.startCopyTable("TABLE_$tables", totalRows, "")

        for (rows in step..totalRows step step) {
          indicator.startExecution("foo")

          if (rows % (step * 10) == 0) {
            indicator.info("Huiuiuiui $rows")
          }

          ThreadUtils.sleep(Duration.ofMillis(50))
          indicator.endExecution(rows)
        }
      }

      indicator.endProcess()
      indicator.finalizeIndicator()
    }
  }
}