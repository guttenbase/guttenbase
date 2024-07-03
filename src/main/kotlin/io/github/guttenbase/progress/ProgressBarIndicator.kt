package io.github.guttenbase.progress

import io.github.guttenbase.utils.Util.abbreviate

class ProgressBarIndicator: TableCopyProgressIndicator {
  override fun initializeIndicator() {
    print(CLEAR_SCREEN)
  }

  override fun finalizeIndicator() {
    println("\nDone")
  }

  override fun startProcess(numberOfTables: Int) {
    val prefix = "%15s (%03d/%03d) ".format( "Länger als 15 Zeichen".abbreviate(15), 0, numberOfTables)
    println(prefix + progressBar(20, 0))
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {

  }

  override fun startExecution() {
    TODO("Not yet implemented")
  }

  override fun endExecution(totalCopiedRows: Int) {
    TODO("Not yet implemented")
  }

  override fun endProcess() {
    TODO("Not yet implemented")
  }

  override fun updateTimers() {
    TODO("Not yet implemented")
  }

  override fun warn(text: String) {
  }

  override fun info(text: String) {
  }

  override fun debug(text: String) {
  }

  @Suppress("MemberVisibilityCanBePrivate")
  companion object{
    const val CHAR_ESCAPE = ''
    const val CHAR_INCOMPLETE = '░' // U+2591 Unicode Character
    const val CHAR_COMPLETED = '█'  // U+2588 Unicode Character
    const val ESCAPE_START = "$CHAR_ESCAPE["
    const val ERASE_RIGHT = "${ESCAPE_START}0K"
    const val ERASE_SCREEN = "${ESCAPE_START}2J"
    const val HOME = "${ESCAPE_START}H"
    const val CLEAR_SCREEN = "${HOME}$ERASE_SCREEN"

    fun linesUp(n:Int) = "${ESCAPE_START}${n}F"
    fun linesDown(n:Int) = "${ESCAPE_START}${n}E"

    fun progressBar(incomplete: Int, complete:Int) = "$CHAR_INCOMPLETE".repeat(incomplete) + "$CHAR_COMPLETED".repeat(complete)

    @JvmStatic
    fun main(args: Array<String>) {
      val indicator = ProgressBarIndicator()
      indicator.initializeIndicator()
      indicator.startProcess(15)
      indicator.finalizeIndicator()
    }
  }
}