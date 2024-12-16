package io.github.guttenbase.hints.impl

import io.github.guttenbase.hints.TableOrderHint
import io.github.guttenbase.mapping.TableOrderComparatorFactory
import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.tools.TableOrderTool

/**
 * By default order by foreign key dependency, i.e. top-down from main tables to depending tables
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DefaultTableOrderHint : TableOrderHint() {
  private lateinit var tables: List<TableMetaData>

  override val value: TableOrderComparatorFactory
    get() = TableOrderComparatorFactory {
      Comparator { table1, table2 ->
        assert(table1.databaseMetaData === table2.databaseMetaData)

        if (!this@DefaultTableOrderHint::tables.isInitialized) {
          tables = TableOrderTool(table1.databaseMetaData).orderTables()
        }

        val index1 = tables.indexOf(table1)
        val index2 = tables.indexOf(table2)

        index1 - index2
      }
    }
}
