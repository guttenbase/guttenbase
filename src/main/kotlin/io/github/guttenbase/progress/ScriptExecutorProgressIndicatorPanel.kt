package io.github.guttenbase.progress

import java.awt.*
import javax.swing.*
import javax.swing.border.EtchedBorder
import javax.swing.border.TitledBorder

/**
 * &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class ScriptExecutorProgressIndicatorPanel : JPanel() {
  val totalTime: JTextField
  val scriptTime: JTextField
  val totalProgress: JProgressBar
  val messages: JTextArea

  /**
   * Create the panel.
   */
  init {
    val gridBagLayout = GridBagLayout()
    gridBagLayout.columnWidths = intArrayOf(0, 0, 0, 0, 0, 0, 0)
    gridBagLayout.rowHeights = intArrayOf(0, 0, 0, 0, 0, 0)
    gridBagLayout.columnWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE)
    gridBagLayout.rowWeights = doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE)
    layout = gridBagLayout
    val lblTotalTimeElapsed = JLabel("Total time elapsed")
    lblTotalTimeElapsed.font = Font("Tahoma", Font.BOLD, 12)
    val gbcLabelTotalTimeElapsed = GridBagConstraints()
    gbcLabelTotalTimeElapsed.anchor = GridBagConstraints.WEST
    gbcLabelTotalTimeElapsed.insets = Insets(5, 5, 5, 5)
    gbcLabelTotalTimeElapsed.gridx = 0
    gbcLabelTotalTimeElapsed.gridy = 1
    add(lblTotalTimeElapsed, gbcLabelTotalTimeElapsed)
    totalTime = JTextField()
    totalTime.isEditable = false
    totalTime.columns = 10
    val gbc_totalTime = GridBagConstraints()
    gbc_totalTime.weightx = 1.0
    gbc_totalTime.insets = Insets(0, 0, 5, 5)
    gbc_totalTime.anchor = GridBagConstraints.WEST
    gbc_totalTime.fill = GridBagConstraints.HORIZONTAL
    gbc_totalTime.gridx = 1
    gbc_totalTime.gridy = 1
    add(totalTime, gbc_totalTime)
    val lblTableTimeElapsed = JLabel("Statement time elapsed")
    lblTableTimeElapsed.font = Font("Tahoma", Font.BOLD, 12)
    val gbc_lblTableTimeElapsed = GridBagConstraints()
    gbc_lblTableTimeElapsed.anchor = GridBagConstraints.WEST
    gbc_lblTableTimeElapsed.insets = Insets(5, 5, 5, 5)
    gbc_lblTableTimeElapsed.gridx = 3
    gbc_lblTableTimeElapsed.gridy = 1
    add(lblTableTimeElapsed, gbc_lblTableTimeElapsed)
    scriptTime = JTextField()
    scriptTime.isEditable = false
    scriptTime.columns = 10
    val gbc_tableTime = GridBagConstraints()
    gbc_tableTime.insets = Insets(0, 0, 5, 5)
    gbc_tableTime.weightx = 1.0
    gbc_tableTime.gridwidth = 1
    gbc_tableTime.fill = GridBagConstraints.HORIZONTAL
    gbc_tableTime.anchor = GridBagConstraints.WEST
    gbc_tableTime.gridx = 4
    gbc_tableTime.gridy = 1
    add(scriptTime, gbc_tableTime)
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
    gbc_panel_1.gridy = 2
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
    gbc_panel_2.gridy = 3
    add(panel_2, gbc_panel_2)
    panel_2.layout = BorderLayout(0, 0)
    val scrollPane = JScrollPane()
    panel_2.add(scrollPane, BorderLayout.CENTER)
    messages = JTextArea()
    messages.rows = 20
    scrollPane.setViewportView(messages)
  }

  companion object {
    private const val serialVersionUID = 1L
  }
}
