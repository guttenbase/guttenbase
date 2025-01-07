package io.github.guttenbase.export.zip


import io.github.guttenbase.meta.ColumnMetaData
import io.github.guttenbase.meta.InternalColumnMetaData
import java.io.IOException

/**
 * Write ZIP file entry containing information about a table column such as type and name.
 *
 * &copy; 2012-2044 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipColumnMetaDataWriter : ZipAbstractMetaDataWriter() {
  @Throws(IOException::class)
  fun writeColumnMetaDataEntry(columnMetaData: ColumnMetaData): ZipColumnMetaDataWriter {
    setProperty(COLUMN_NAME, columnMetaData.columnName)
    setProperty(COLUMN_CLASS_NAME, columnMetaData.columnClassName)
    setProperty(COLUMN_TYPE_NAME, columnMetaData.columnTypeName)
    setProperty(COLUMN_TYPE, java.lang.String.valueOf(columnMetaData.columnType))
    setProperty(COLUMN_PRECISION, java.lang.String.valueOf(columnMetaData.precision))
    setProperty(COLUMN_SCALE, java.lang.String.valueOf(columnMetaData.scale))
    setProperty(PRIMARY_KEY, java.lang.String.valueOf(columnMetaData.isPrimaryKey))
    setProperty(NULLABLE, java.lang.String.valueOf(columnMetaData.isNullable))
    setProperty(AUTO_INCREMENT, java.lang.String.valueOf(columnMetaData.isAutoIncrement))
    setProperty(COLUMN_ID, java.lang.String.valueOf((columnMetaData as InternalColumnMetaData).columnId))
    setFkProperties(columnMetaData.referencedColumns, COLUMN_FK_REFERENCES_IDS_SUFFIX, COLUMN_FK_REFERENCES_SUFFIX)
    setFkProperties(columnMetaData.referencingColumns, COLUMN_FK_REFERENCED_BY_IDS_SUFFIX, COLUMN_FK_REFERENCED_BY_SUFFIX)

    return this
  }

  private fun setFkProperties(
    fkColumns: Map<String, List<ColumnMetaData>>, columnFkReferencesIdsSuffix: String, columnFkReferencesSuffix: String
  ) {
    for ((key, columns) in fkColumns) {
      val ids = mapToIds(columns)
      val names = mapToNames(columns)
      setProperty("$key.$columnFkReferencesIdsSuffix", ids)
      setProperty("$key.$columnFkReferencesSuffix", names)
    }
  }

  companion object {
    const val COLUMN_NAME = "Name"
    const val COLUMN_ID = "Column-Id"
    const val COLUMN_CLASS_NAME = "Class-Name"
    const val COLUMN_TYPE_NAME = "Type-Name"
    const val COLUMN_TYPE = "JDBC-Type"
    const val COLUMN_PRECISION = "Precision"
    const val COLUMN_SCALE = "Scale"
    const val COLUMN_FK_REFERENCES_IDS_SUFFIX = ".References-Ids"
    const val COLUMN_FK_REFERENCES_SUFFIX = "References"
    const val COLUMN_FK_REFERENCED_BY_IDS_SUFFIX = "Referenced-By-Ids"
    const val COLUMN_FK_REFERENCED_BY_SUFFIX = "Referenced-By"
    const val PRIMARY_KEY = "Primary-Key"
    const val NULLABLE = "Nullable"
    const val AUTO_INCREMENT = "Auto-Increment"

    private fun mapToNames(columns: List<ColumnMetaData>) =
      columns.joinToString(separator = ", ", transform = { it.columnName + " (" + it.tableMetaData.tableName + ")" })

    private fun mapToIds(columns: List<ColumnMetaData>) =
      columns.joinToString(separator = ", ", transform = { (it as InternalColumnMetaData).columnId.toString() })
  }
}
