package io.github.guttenbase.tools

import io.github.guttenbase.meta.TableMetaData

/**
 * Topologically sort tables by foreign key constraints, i.e. the foreign keys of a database schema spawn an directed (possibly cyclic!) graph
 * of dependencies. The tool tries to create of sequential order either in top-down (starting at the root nodes) or bottom-up
 * (starting at the leaves) manner.
 *
 * If there are cycles in the dependencies, we choose the node with the fewest incoming/outgoing edges.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class TableOrderTool {
  fun getOrderedTables(tableMetaData: List<TableMetaData>, topDown: Boolean = true): List<TableMetaData> {
    val tableNodes = createGraph(tableMetaData)

    return orderTables(tableNodes, topDown)
  }

  private fun orderTables(tableNodes: MutableMap<String, TableNode>, topDown: Boolean): List<TableMetaData> {
    val result = ArrayList<TableMetaData>()

    while (tableNodes.isNotEmpty()) {
      val tableNode = findMatchingNode(ArrayList(tableNodes.values), topDown)

      for (referencingTable in tableNode.referencedByTables) {
        referencingTable.removeReferencedTable(tableNode)
      }

      for (referencedTable in tableNode.referencedTables) {
        referencedTable.removeReferencedByTable(tableNode)
      }

      result.add(tableNode.tableMetaData)
      tableNodes.remove(tableNode.tableMetaData.tableName.uppercase())
    }

    return result
  }

  private fun findMatchingNode(tableNodes: List<TableNode>, topDown: Boolean) =
    tableNodes.sortedWith { tn1: TableNode, tn2: TableNode ->
      if (topDown) {
        tn1.referencedTables.size - tn2.referencedTables.size
      } else {
        tn1.referencedByTables.size - tn2.referencedByTables.size
      }
    }[0]

  private fun createGraph(tableMetaData: List<TableMetaData>): MutableMap<String, TableNode> {
    val tableNodes = LinkedHashMap<String, TableNode>()

    for (table in tableMetaData) {
      val importedForeignKeys = table.importedForeignKeys
      val tableNode = getTableNode(tableNodes, table)

      for (foreignKeyMetaData in importedForeignKeys) {
        val referencingTable = getTableNode(tableNodes, foreignKeyMetaData.referencingTableMetaData)
        val referencedTable = getTableNode(tableNodes, foreignKeyMetaData.referencedTableMetaData)

        assert(tableNode == referencingTable)

        referencingTable.addReferencedTable(referencedTable)
        referencedTable.addReferencedByTable(referencingTable)
      }
    }

    return tableNodes
  }

  private fun getTableNode(tableNodes: MutableMap<String, TableNode>, table: TableMetaData) =
    tableNodes.getOrPut(table.tableName.uppercase()) { TableNode(table) }

  private data class TableNode(val tableMetaData: TableMetaData) {
    val referencedTables = ArrayList<TableNode>()
    val referencedByTables = ArrayList<TableNode>()

    fun addReferencedTable(tableMetaData: TableNode) {
      referencedTables.add(tableMetaData)
    }

    fun removeReferencedTable(tableMetaData: TableNode) {
      referencedTables.remove(tableMetaData)
    }

    fun addReferencedByTable(tableMetaData: TableNode) {
      referencedByTables.add(tableMetaData)
    }

    fun removeReferencedByTable(tableMetaData: TableNode) {
      referencedByTables.remove(tableMetaData)
    }

    override fun hashCode() = tableMetaData.hashCode()

    override fun equals(other: Any?): Boolean {
      val that = other as TableNode
      return this.tableMetaData == that.tableMetaData
    }

    override fun toString(): String {
      return (tableMetaData.tableName + "::referencedTables:"
          + toString(referencedByTables)
          + ", referencedByTables: "
          + toString(referencedByTables))
    }

    companion object {
      private fun toString(referencedTables: List<TableNode>) =
        referencedTables.joinToString(prefix = "[", postfix = "]", transform = { it.tableMetaData.tableName })
    }
  }
}
