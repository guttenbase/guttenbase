package io.github.guttenbase.progress

import io.github.guttenbase.utils.Util.abbreviate
import org.apache.commons.io.ThreadUtils
import org.slf4j.LoggerFactory
import java.time.Duration

class TableCopyProgressBarIndicator
@JvmOverloads constructor(
  private val messageLength: Int = 20,
  private val progressBarLength: Int = 50
) : TableCopyProgressIndicator {
  private var totalTableCount = 0
  private var tableCount = 0
  private var totalRowCount = 0
  private var rowCount = 0
  private var tableName = ""
  private val initialProgressbar = progressBar(progressBarLength, 0)

  override fun initializeIndicator() {
    print(CLEAR_SCREEN)
  }

  override fun finalizeIndicator() {
  }

  override fun startProcess(numberOfTables: Int) {
    this.totalTableCount = numberOfTables

    val overallProgress = status(messageLength, totalTableCount, 0, "Tables", initialProgressbar)
    val currentProgress = status(messageLength, 0, 0, "Rows", initialProgressbar)

    println(overallProgress)
    println(currentProgress)
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {
    this.tableName = stripSchema(sourceTableName)
    this.totalRowCount = rowCount
    this.rowCount = 0

    val progressBar = progressbar(progressBarLength, tableCount, totalTableCount)
    val overallProgress = status(messageLength, totalTableCount, ++tableCount, "Tables", progressBar)
    val currentProgress = status(messageLength, totalRowCount, this.rowCount, tableName, initialProgressbar)

    print(linesUp(2) + ERASE_RIGHT)
    println(overallProgress)
    println(currentProgress)
  }

  override fun startExecution(action: String) {
  }

  override fun endExecution(totalCopiedRows: Int) {
    this.rowCount = totalCopiedRows

    val progressBar = progressbar(progressBarLength, rowCount, totalRowCount)
    val currentProgress = status(messageLength, totalRowCount, this.rowCount, tableName, progressBar)

    print(linesUp(1) + ERASE_RIGHT)
    println(currentProgress)
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
    // https://gist.github.com/fnky/458719343aabd01cfb17a3a4f7296797
    const val CHAR_ESCAPE = ''
    const val CHAR_INCOMPLETE = '░' // U+2591 Unicode Character
    const val CHAR_COMPLETED = '█'  // U+2588 Unicode Character
    const val ESCAPE_START = "$CHAR_ESCAPE["
    const val ERASE_RIGHT = "${ESCAPE_START}0K"
    const val ERASE_SCREEN = "${ESCAPE_START}2J"
    const val HOME = "${ESCAPE_START}H"
    const val CLEAR_SCREEN = "${HOME}$ERASE_SCREEN"

    @JvmStatic
    internal val PROGRESS_LOG = LoggerFactory.getLogger(TableCopyProgressIndicator::class.java)

    internal fun linesUp(n: Int) = "${ESCAPE_START}${n}F"

    internal fun progressBar(incomplete: Int, complete: Int) =
      "$CHAR_COMPLETED".repeat(complete) + "$CHAR_INCOMPLETE".repeat(incomplete)

    internal fun progressbar(progressBarLength: Int, count: Int, totalCount: Int): String {
      val percentage = count / totalCount.toDouble()
      val completed = (percentage * progressBarLength).toInt()
      val incomplete = progressBarLength - completed
      val progressBar = progressBar(incomplete, completed)

      return progressBar
    }

    internal fun status(messageLength: Int, totalCount: Int, count: Int, text: String, progressBar: String): String {
      val digits = totalCount.toString().length
      val digitFormat = "%0${digits}d"

      return "%-${messageLength}s %50s ($digitFormat/$digitFormat)".format(
        text.abbreviate(messageLength), progressBar, count, totalCount
      )
    }

    internal fun stripSchema(name: String) = when (val index = name.lastIndexOf('.')) {
      -1 -> name
      else -> name.substring(index + 1)
    }

    internal fun stripNewlines(text: String) = text.replace("\n", "\t")

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

