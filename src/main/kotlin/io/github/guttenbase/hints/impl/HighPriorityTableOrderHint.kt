package io.github.guttenbase.hints.impl

import io.github.guttenbase.mapping.TableOrderComparatorFactory
import kotlin.Comparator


/**
 * Order by natural order of table names with some high priority exceptions ranked first
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 * @author M. Dahm
 */
@Suppress("unused")
open class HighPriorityTableOrderHint(tableNames: Collection<String>) : DefaultTableOrderHint() {
  private val tableNames = tableNames.map { it.uppercase() }

  constructor(vararg names: String): this(names.toList())

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
