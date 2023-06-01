package io.github.guttenbase.hints.impl

import io.github.guttenbase.mapping.TableOrderComparatorFactory
import kotlin.Comparator


/**
 * Order by natural order of table names with some high priority exceptions ranked first
 *
 *  2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
class HighPriorityTableOrderHint(vararg tableNames: String) : DefaultTableOrderHint() {
  private val tableNames = tableNames.map { it.uppercase() }

  override val value: TableOrderComparatorFactory
    get() = TableOrderComparatorFactory {
      Comparator { t1, t2 ->
        if (tableNames.contains(t1.tableName.uppercase())) {
           -1
        } else if (tableNames.contains(t2.tableName.uppercase())) {
           1
        } else {
          t1.compareTo(t2)
        }
      }
    }
}
