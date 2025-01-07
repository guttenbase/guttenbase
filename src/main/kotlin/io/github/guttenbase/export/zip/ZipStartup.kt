package io.github.guttenbase.export.zip

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.FlowLayout
import java.io.IOException
import java.io.ObjectInputStream
import java.sql.SQLException
import java.util.*
import javax.swing.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import kotlin.system.exitProcess

/**
 * Default tool to start when "executing" the JAR file. It simply displays the file structure.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipStartup : JPanel(BorderLayout()) {
  @Throws(Exception::class)
  fun initGUI() {
    add(createButtonPanel(), BorderLayout.SOUTH)
    val treeModel: DefaultTreeModel = createTreeModel()
    add(JScrollPane(JTree(treeModel)), BorderLayout.CENTER)
  }

  @Throws(Exception::class)
  private fun createTreeModel(): DefaultTreeModel {
    val databaseMetaData = readDatabaseMetaData()
    val rootNode: DefaultMutableTreeNode = addRootNode(databaseMetaData)

    addTableNodes(databaseMetaData, rootNode)

    return DefaultTreeModel(rootNode)
  }

  @Throws(IOException::class, ClassNotFoundException::class)
  private fun readDatabaseMetaData(): DatabaseMetaData {
    val inputStream =
      ZipExporter::class.java.getResourceAsStream(ZipConstants.PATH_SEPARATOR.toString() + ZipConstants.META_DATA)
    val objectInputStream = ObjectInputStream(inputStream)
    val databaseMetaData = objectInputStream.readObject() as DatabaseMetaData
    objectInputStream.close()

    return databaseMetaData
  }

  @Throws(IOException::class, SQLException::class)
  private fun addRootNode(databaseMetaData: DatabaseMetaData): DefaultMutableTreeNode {
    val rootNode = DefaultMutableTreeNode(ZipConstants.GUTTEN_BASE_NAME)
    val databaseMetaDataProperties = ZipDatabaseMetaDataWriter().writeDatabaseMetaDataEntry(databaseMetaData)
      .properties
    addMetaDataProperties(databaseMetaDataProperties, rootNode, ZipDatabaseMetaDataWriter.TABLE_NAME)
    return rootNode
  }

  @Throws(IOException::class)
  private fun addTableNodes(databaseMetaData: DatabaseMetaData, rootNode: DefaultMutableTreeNode) {
    val tableMetaDatas: List<TableMetaData> = databaseMetaData.tableMetaData

    for (tableMetaData in tableMetaDatas) {
      val tableMetaDataProperties = ZipTableMetaDataWriter().writeTableMetaDataEntry(tableMetaData).properties
      val tableNode = DefaultMutableTreeNode(tableMetaData.tableName)
      addMetaDataProperties(tableMetaDataProperties, tableNode, "XX")
      rootNode.add(tableNode)
      addColumnNodes(tableMetaData, tableNode)
      addIndexNodes(tableMetaData, tableNode)
    }
  }

  @Throws(IOException::class)
  private fun addColumnNodes(tableMetaData: TableMetaData, tableNode: DefaultMutableTreeNode) {
    for (columnMetaData in tableMetaData.columnMetaData) {
      val columnMetaDataProperties = ZipColumnMetaDataWriter().writeColumnMetaDataEntry(columnMetaData).properties
      val columnNode = DefaultMutableTreeNode(columnMetaData.columnName)

      addMetaDataProperties(columnMetaDataProperties, columnNode, "XX")
      tableNode.add(columnNode)
    }
  }

  @Throws(IOException::class)
  private fun addIndexNodes(tableMetaData: TableMetaData, tableNode: DefaultMutableTreeNode) {
    val indexesNode = DefaultMutableTreeNode("Indexes")
    tableNode.add(indexesNode)
    for (indexMetaData in tableMetaData.indexes) {
      val columnMetaDataProperties = ZipIndexMetaDataWriter().writeIndexMetaDataEntry(indexMetaData).properties
      val indexNode = DefaultMutableTreeNode(indexMetaData.indexName)
      addMetaDataProperties(columnMetaDataProperties, indexNode, "XX")
      indexesNode.add(indexNode)
    }
  }

  @Throws(IOException::class)
  private fun addMetaDataProperties(metaDataProperties: Properties, rootNode: DefaultMutableTreeNode, excludedProperty: String) {
    val keysEnum = metaDataProperties.keys()

    while (keysEnum.hasMoreElements()) {
      val key: String = keysEnum.nextElement().toString()
      val value = metaDataProperties.getProperty(key)

      if (!key.startsWith(excludedProperty)) {
        rootNode.add(DefaultMutableTreeNode("$key: $value"))
      }
    }
  }

  private fun createButtonPanel(): JPanel {
    val buttonPanel = JPanel(FlowLayout())
    val close = JButton("Close")
    close.addActionListener { exitProcess(0) }
    buttonPanel.add(close)
    return buttonPanel
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      try {
        val frame = JFrame("GuttenBase GUI")
        val startup = ZipStartup()
        frame.contentPane = startup
        startup.initGUI()
        val size = Dimension(1200, 800)
        frame.minimumSize = size
        frame.size = size
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.pack()
        frame.isVisible = true
      } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(0)
      }
    }
  }
}
