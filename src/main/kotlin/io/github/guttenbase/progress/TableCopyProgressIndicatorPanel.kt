package io.github.guttenbase.progress

import java.awt.*
import javax.swing.*
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * &copy; 2012-2044 tech@spree
 *
 * @author M. Dahm
 */
class TableCopyProgressIndicatorPanel : JPanel() {
  val sourceTable: JTextField
  val targetTable: JTextField
  val totalTime: JTextField
  val tableTime: JTextField
  val totalProgress: JProgressBar
  val tableProgress: JProgressBar
  val messages: JTextArea

  /**
   * Create the panel.
   */
  init {
    val gridBagLayout = GridBagLayout()
    gridBagLayout.columnWidths = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    gridBagLayout.rowHeights = intArrayOf(0, 0, 0, 0, 0, 0, 0, 0)
    gridBagLayout.columnWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE)
    gridBagLayout.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE)
    layout = gridBagLayout
    val lblNewLabel = JLabel("Source table")
    lblNewLabel.horizontalAlignment = SwingConstants.LEFT
    lblNewLabel.font = Font("Tahoma", Font.BOLD, 12)
    val gbc_lblNewLabel = GridBagConstraints()
    gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL
    gbc_lblNewLabel.anchor = GridBagConstraints.WEST
    gbc_lblNewLabel.insets = Insets(5, 5, 5, 5)
    gbc_lblNewLabel.gridx = 0
    gbc_lblNewLabel.gridy = 1
    add(lblNewLabel, gbc_lblNewLabel)
    sourceTable = JTextField()
    sourceTable.isEditable = false
    val gbcSourceTable = GridBagConstraints()
    gbcSourceTable.insets = Insets(0, 0, 5, 5)
    gbcSourceTable.gridwidth = 2
    gbcSourceTable.fill = GridBagConstraints.HORIZONTAL
    gbcSourceTable.anchor = GridBagConstraints.WEST
    gbcSourceTable.gridx = 1
    gbcSourceTable.gridy = 1
    add(sourceTable, gbcSourceTable)
    sourceTable.columns = 40
    val lblTargetTable = JLabel("Target table")
    lblTargetTable.font = Font("Tahoma", Font.BOLD, 12)
    val gbc_lblTargetTable = GridBagConstraints()
    gbc_lblTargetTable.anchor = GridBagConstraints.WEST
    gbc_lblTargetTable.insets = Insets(5, 5, 5, 5)
    gbc_lblTargetTable.gridx = 3
    gbc_lblTargetTable.gridy = 1
    add(lblTargetTable, gbc_lblTargetTable)
    targetTable = JTextField()
    targetTable.isEditable = false
    targetTable.columns = 40
    val gbc_targetTable = GridBagConstraints()
    gbc_targetTable.insets = Insets(0, 0, 5, 0)
    gbc_targetTable.weightx = 1.0
    gbc_targetTable.anchor = GridBagConstraints.WEST
    gbc_targetTable.gridwidth = 2
    gbc_targetTable.fill = GridBagConstraints.HORIZONTAL
    gbc_targetTable.gridx = 4
    gbc_targetTable.gridy = 1
    add(targetTable, gbc_targetTable)
    val lblTotalTimeElapsed = JLabel("Total time elapsed")
    lblTotalTimeElapsed.font = Font("Tahoma", Font.BOLD, 12)
    val gbc_lblTotalTimeElapsed = GridBagConstraints()
    gbc_lblTotalTimeElapsed.anchor = GridBagConstraints.WEST
    gbc_lblTotalTimeElapsed.insets = Insets(5, 5, 5, 5)
    gbc_lblTotalTimeElapsed.gridx = 0
    gbc_lblTotalTimeElapsed.gridy = 2
    add(lblTotalTimeElapsed, gbc_lblTotalTimeElapsed)
    totalTime = JTextField()
    totalTime.isEditable = false
    totalTime.columns = 10
    val gbc_totalTime = GridBagConstraints()
    gbc_totalTime.weightx = 1.0
    gbc_totalTime.insets = Insets(0, 0, 5, 5)
    gbc_totalTime.anchor = GridBagConstraints.WEST
    gbc_totalTime.fill = GridBagConstraints.HORIZONTAL
    gbc_totalTime.gridx = 1
    gbc_totalTime.gridy = 2
    add(totalTime, gbc_totalTime)
    val lblTableTimeElapsed = JLabel("Table time elapsed")
    lblTableTimeElapsed.font = Font("Tahoma", Font.BOLD, 12)
    val gbc_lblTableTimeElapsed = GridBagConstraints()
    gbc_lblTableTimeElapsed.anchor = GridBagConstraints.WEST
    gbc_lblTableTimeElapsed.insets = Insets(5, 5, 5, 5)
    gbc_lblTableTimeElapsed.gridx = 3
    gbc_lblTableTimeElapsed.gridy = 2
    add(lblTableTimeElapsed, gbc_lblTableTimeElapsed)
    tableTime = JTextField()
    tableTime.isEditable = false
    tableTime.columns = 10
    val gbc_tableTime = GridBagConstraints()
    gbc_tableTime.insets = Insets(0, 0, 5, 5)
    gbc_tableTime.weightx = 1.0
    gbc_tableTime.gridwidth = 1
    gbc_tableTime.fill = GridBagConstraints.HORIZONTAL
    gbc_tableTime.anchor = GridBagConstraints.WEST
    gbc_tableTime.gridx = 4
    gbc_tableTime.gridy = 2
    add(tableTime, gbc_tableTime)
    val panel = JPanel()
    panel.border = TitledBorder(EtchedBorder(), "Table rows", TitledBorder.LEADING, TitledBorder.TOP, null, null)
    val gbc_panel = GridBagConstraints()
    gbc_panel.weighty = 0.2
    gbc_panel.gridwidth = 6
    gbc_panel.anchor = GridBagConstraints.WEST
    gbc_panel.insets = Insets(5, 5, 5, 0)
    gbc_panel.fill = GridBagConstraints.BOTH
    gbc_panel.gridx = 0
    gbc_panel.gridy = 3
    add(panel, gbc_panel)
    panel.layout = BorderLayout(0, 0)
    tableProgress = JProgressBar()
    tableProgress.isStringPainted = true
    panel.add(tableProgress, BorderLayout.CENTER)
    val panel_1 = JPanel()
    panel_1.border = TitledBorder(
      EtchedBorder(EtchedBorder.LOWERED, null, null), "Total progress",
      TitledBorder.LEADING, TitledBorder.TOP, null, null
    )
    val gbc_panel_1 = GridBagConstraints()
    gbc_panel_1.weighty = 0.2
    gbc_panel_1.anchor = GridBagConstraints.WEST
    gbc_panel_1.gridwidth = 6
    gbc_panel_1.insets = Insets(5, 5, 5, 0)
    gbc_panel_1.fill = GridBagConstraints.BOTH
    gbc_panel_1.gridx = 0
    gbc_panel_1.gridy = 4
    add(panel_1, gbc_panel_1)
    panel_1.layout = BorderLayout(0, 0)
    totalProgress = JProgressBar()
    totalProgress.isStringPainted = true
    panel_1.add(totalProgress, BorderLayout.CENTER)
    val panel_2 = JPanel()
    panel_2.border = TitledBorder(
      EtchedBorder(EtchedBorder.LOWERED, null, null), "Messages", TitledBorder.LEADING,
      TitledBorder.TOP, null, null
    )
    val gbc_panel_2 = GridBagConstraints()
    gbc_panel_2.weighty = 1.0
    gbc_panel_2.weightx = 1.0
    gbc_panel_2.gridheight = 2
    gbc_panel_2.gridwidth = 6
    gbc_panel_2.insets = Insets(5, 5, 5, 5)
    gbc_panel_2.fill = GridBagConstraints.BOTH
    gbc_panel_2.gridx = 0
    gbc_panel_2.gridy = 5
    add(panel_2, gbc_panel_2)
    panel_2.layout = BorderLayout(0, 0)
    val scrollPane = JScrollPane()
    panel_2.add(scrollPane, BorderLayout.CENTER)
    messages = JTextArea()
    messages.rows = 20
    scrollPane.setViewportView(messages)
  }
}
