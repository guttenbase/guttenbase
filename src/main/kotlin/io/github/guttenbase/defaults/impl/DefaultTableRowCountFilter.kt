package io.github.guttenbase.defaults.impl

import io.github.guttenbase.meta.TableMetaData
import io.github.guttenbase.repository.TableRowCountFilter

/**
 * By default compute row count for all tables
 */
open class DefaultTableRowCountFilter : TableRowCountFilter {
  override fun defaultRowCount(tableMetaData: TableMetaData) = 0
  override fun defaultMaxId(tableMetaData: TableMetaData) = 0L
  override fun defaultMinId(tableMetaData: TableMetaData) = 0L
}
