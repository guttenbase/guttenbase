package io.github.guttenbase.export.zip


import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.IndexMetaData
import io.github.guttenbase.meta.InternalColumnMetaData
import java.io.IOException
import kotlin.Throws

/**
 * Write ZIP file entry containing information about a table column index such as name and columns.
 *
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipIndexMetaDataWriter : ZipAbstractMetaDataWriter() {
  @Throws(IOException::class)
  fun writeIndexMetaDataEntry(indexMetaData: IndexMetaData): ZipIndexMetaDataWriter {
    setProperty(INDEX_NAME, indexMetaData.indexName)
    setProperty(ASCENDING, java.lang.String.valueOf(indexMetaData.isAscending))
    setProperty(UNIQUE, java.lang.String.valueOf(indexMetaData.isUnique))

    var i = 1
    val iterator: Iterator<ColumnMetaData> = indexMetaData.columnMetaData.iterator()

    while (iterator.hasNext()) {
      val columnMetaData: InternalColumnMetaData = iterator.next() as InternalColumnMetaData
      setProperty(COLUMN_ID + i, java.lang.String.valueOf(columnMetaData.columnId))
      setProperty(COLUMN + i, columnMetaData.columnName)
      i++
    }

    return this
  }

  companion object {
    const val INDEX_NAME = "Index-Name"
    const val COLUMN = "Index-Column"
    const val COLUMN_ID = "Index-Column-Id"
    const val ASCENDING = "Ascending"
    const val UNIQUE = "Unique"
  }
}
