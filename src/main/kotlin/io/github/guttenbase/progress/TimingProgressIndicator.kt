package io.github.guttenbase.progress

/**
 * Record timings.
 *
 * &copy; 2013-2020 akquinet tech@spree
 *
 * @author M. Dahm
 */
object TimingProgressIndicator : TableCopyProgressIndicator {
  private var startTotalTime: Long = 0
  private var startProcessTime: Long = 0
  private var startExecutionTime: Long = 0

  var itemCounter = 0
    private set
  var sourceTableName: String? = null
    private set
  var targetTableName: String? = null
    private set
  var rowCount = 0
    private set
  var numberOfTables = 0
    private set
  var elapsedExecutionTime: Long = 0
    private set
  var elapsedProcessTime: Long = 0
    private set
  var elapsedTotalTime: Long = 0
    private set

  override fun initializeIndicator() {}

  override fun startProcess(numberOfTables: Int) {
    setNumberOfItems(numberOfTables)
    itemCounter = 1
    startTotalTime = System.currentTimeMillis()
    startProcessTime = System.currentTimeMillis()
  }

  override fun startCopyTable(sourceTableName: String, rowCount: Int, targetTableName: String) {
    this.sourceTableName = sourceTableName
    this.rowCount = rowCount
    this.targetTableName = targetTableName

    startProcessTime = System.currentTimeMillis()
  }

  override fun startExecution(action: String) {
    startExecutionTime = System.currentTimeMillis()
  }

  override fun endExecution(totalCopiedRows: Int) {
    updateTimers()
  }

  override fun endProcess() {
    updateTimers()
    itemCounter++
  }

  override fun warn(text: String) {}
  override fun info(text: String) {}
  override fun debug(text: String) {}

  override fun finalizeIndicator() {
    updateTimers()
  }

  private fun setNumberOfItems(numberOfTables: Int) {
    this.numberOfTables = numberOfTables
  }

  override fun updateTimers() {
    val millis = System.currentTimeMillis()

    elapsedExecutionTime = millis - startExecutionTime
    elapsedTotalTime = millis - startTotalTime
    elapsedProcessTime = millis - startProcessTime
  }
}
