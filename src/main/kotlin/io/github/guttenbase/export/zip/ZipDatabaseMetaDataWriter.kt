package io.github.guttenbase.export.zip


import io.github.guttenbase.meta.DatabaseMetaData
import java.io.IOException
import java.sql.SQLException
import kotlin.Throws

/**
 * Write ZIP file entry containing information about data base such as schema or version.
 *
 *
 *  2012-2034 akquinet tech@spree
 *
 *
 * @author M. Dahm
 */
class ZipDatabaseMetaDataWriter : ZipAbstractMetaDataWriter() {
  @Throws(IOException::class, SQLException::class)
  fun writeDatabaseMetaDataEntry(databaseMetaData: DatabaseMetaData): ZipDatabaseMetaDataWriter {
    setProperty(DATABASE_SCHEMA, databaseMetaData.schema)
    setProperty(DATABASE_NAME, databaseMetaData.databaseMetaData.databaseProductName)
    setProperty(DATABASE_MAJOR_VERSION, java.lang.String.valueOf(databaseMetaData.databaseMetaData.databaseMajorVersion))
    setProperty(DATABASE_MINOR_VERSION, java.lang.String.valueOf(databaseMetaData.databaseMetaData.databaseMinorVersion))
    setProperty(DATABASE_TYPE, databaseMetaData.databaseType.name)

    var i = 1
    databaseMetaData.tableMetaData.forEach {
      setProperty(TABLE_NAME + (i++), it.tableName)
    }

    return this
  }

  companion object {
    const val DATABASE_NAME = "Database"
    const val DATABASE_TYPE = "Database-Type"
    const val DATABASE_MAJOR_VERSION = "Major-Version"
    const val DATABASE_MINOR_VERSION = "Minor-Version"
    const val DATABASE_SCHEMA = "Database-Schema"
    const val TABLE_NAME = "Table-Name"
  }
}
