package io.github.guttenbase.export.zip


import io.github.guttenbase.meta.TableMetaData
import java.io.IOException
import kotlin.Throws

/**
 * Write ZIP file entry containing information about a table such as name and row count.
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipTableMetaDataWriter : ZipAbstractMetaDataWriter() {
  @Throws(IOException::class)
  fun writeTableMetaDataEntry(tableMetaData: TableMetaData): ZipTableMetaDataWriter {
    setProperty(TABLE_NAME, tableMetaData.tableName)
    setProperty(COLUMN_COUNT, java.lang.String.valueOf(tableMetaData.columnCount))
    setProperty(ROW_COUNT, java.lang.String.valueOf(tableMetaData.totalRowCount))
    return this
  }

  companion object {
    const val TABLE_NAME = "Name"
    const val COLUMN_COUNT = "Column-Count"
    const val ROW_COUNT = "Row-Count"
  }
}
