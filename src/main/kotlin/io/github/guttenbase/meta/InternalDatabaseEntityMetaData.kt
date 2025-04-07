package io.github.guttenbase.meta

interface InternalDatabaseEntityMetaData : DatabaseEntityMetaData {
  override var database: DatabaseMetaData
  override var totalRowCount: Int
}