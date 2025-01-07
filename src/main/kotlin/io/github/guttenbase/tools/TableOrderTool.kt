package io.github.guttenbase.tools

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.meta.TableMetaData

private typealias TableNodes = LinkedHashMap<String, TableNode>

/**
 * Topologically sort tables by foreign key constraints, i.e. the foreign keys of a database schema spawn an directed (possibly cyclic!) graph
 * of dependencies. The tool tries to create of sequential order either in top-down (starting at the root nodes) or bottom-up
 * (starting at the leaves) manner.
 *
 * If there are cycles in the dependencies, we choose the node with the fewest incoming/outgoing edges.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 * @author M. Dahm
 */
class TableOrderTool(private val databaseMetaData: DatabaseMetaData) {
  @JvmOverloads
  fun orderTables(topDown: Boolean = true): List<TableMetaData> {
    val result = ArrayList<TableMetaData>()
    val tableNodes = createGraph(databaseMetaData.tableMetaData)

    while (tableNodes.isNotEmpty()) {
      val tableNode = tableNodes.firstNode(topDown)

      for (referencingTable in tableNode.referencedByTables) {
        referencingTable.removeReferencedTable(tableNode)
      }

      for (referencedTable in tableNode.referencedTables) {
        referencedTable.removeReferencedByTable(tableNode)
      }

      result.add(tableNode.tableMetaData)
      tableNodes.remove(tableNode)
    }

    return result
  }

  private fun TableNodes.remove(table: TableNode) = remove(table.tableMetaData.tableName.uppercase())

  /**
   * Top-Down: Node with least references to other tables is preferred
   * Bottom up: Most referenced node wins
   */
  private fun TableNodes.firstNode(topDown: Boolean) = values.sortedWith { tn1, tn2 ->
    if (topDown) {
      tn1.referencedTables.size - tn2.referencedTables.size
    } else {
      tn1.referencedByTables.size - tn2.referencedByTables.size
    }
  }.first()

  private fun createGraph(tableMetaData: List<TableMetaData>): TableNodes {
    val graph = TableNodes()

    for (table in tableMetaData) {
      val importedForeignKeys = table.importedForeignKeys
      val tableNode = graph.getTableNode(table)

      for (foreignKeyMetaData in importedForeignKeys) {
        val referencingTable = graph.getTableNode(foreignKeyMetaData.referencingTableMetaData)
        val referencedTable = graph.getTableNode(foreignKeyMetaData.referencedTableMetaData)

        assert(tableNode == referencingTable)

        if (referencingTable != referencedTable) { // Ignore self-referencing tables
          referencingTable.addReferencedTable(referencedTable)
          referencedTable.addReferencedByTable(referencingTable)
        }
      }
    }

    return graph
  }
}

private fun TableNodes.getTableNode(table: TableMetaData) = getOrPut(table.tableName.uppercase()) { TableNode(table) }

private class TableNode(val tableMetaData: TableMetaData) {
  val referencedTables = LinkedHashSet<TableNode>()
  val referencedByTables = LinkedHashSet<TableNode>()

  fun addReferencedTable(table: TableNode) {
    referencedTables.add(table)
  }

  fun removeReferencedTable(table: TableNode) {
    referencedTables.remove(table)
  }

  fun addReferencedByTable(table: TableNode) {
    referencedByTables.add(table)
  }

  fun removeReferencedByTable(table: TableNode) {
    referencedByTables.remove(table)
  }

  override fun hashCode() = tableMetaData.hashCode()

  override fun equals(other: Any?): Boolean = other is TableNode && tableMetaData == other.tableMetaData

  override fun toString() = (tableMetaData.tableName +
      "::referencedTables:" + toString(referencedTables)
      + ", referencedByTables: " + toString(referencedByTables))

  companion object {
    private fun toString(tables: Collection<TableNode>) =
      tables.joinToString(prefix = "[", postfix = "]", transform = { it.tableMetaData.tableName })
  }
}
