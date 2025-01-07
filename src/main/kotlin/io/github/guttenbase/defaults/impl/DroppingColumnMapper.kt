package io.github.guttenbase.defaults.impl

import io.github.guttenbase.mapping.ColumnMapper.ColumnMapperResult
import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.TableMetaData
import java.util.*

/**
 * Utility mapper that allows to drop columns in target tables.
 *
 * &copy; 2013 akquinet tech@spree
 *
 * @author M. Dahm
 */
open class DroppingColumnMapper : DefaultColumnMapper() {
  private val droppedColumns = HashMap<String, MutableList<String>>()

  fun addDroppedColumn(targetTableName: String, sourceColumName: String): DroppingColumnMapper {
    val dropped = droppedColumns.getOrPut(targetTableName.uppercase()) { ArrayList() }
    dropped.add(sourceColumName.uppercase())
    return this
  }

  override fun map(source: ColumnMetaData, targetTableMetaData: TableMetaData): ColumnMapperResult {
    val columns: List<String>? = droppedColumns[targetTableMetaData.tableName.uppercase()]

    return if (columns != null && columns.contains(source.columnName.uppercase())) {
      ColumnMapperResult(ArrayList(), true)
    } else {
      super.map(source, targetTableMetaData)
    }
  }
}
