package io.github.guttenbase.hints

import io.github.guttenbase.mapping.TableOrderComparatorFactory

class RandomTableOrderHint : TableOrderHint() {
  override val value: TableOrderComparatorFactory
    get() = TableOrderComparatorFactory { Comparator.comparingInt { System.identityHashCode(it) } }
}
