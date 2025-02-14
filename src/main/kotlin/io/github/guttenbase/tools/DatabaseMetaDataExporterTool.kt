@file:OptIn(ExperimentalSerializationApi::class)

package io.github.guttenbase.tools

import io.github.guttenbase.meta.*
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.serialization.JSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

class DatabaseMetaDataExporterTool(
  private val connectorRepository: ConnectorRepository,
  private val connectorId: String
) {
  private val databaseMetaData: DatabaseMetaData by lazy { connectorRepository.getDatabaseMetaData(connectorId) }

  fun export(file: File) {
    FileOutputStream(file).use {
      JSON.encodeToStream(databaseMetaData, it)
    }
  }

  companion object {
    @JvmOverloads
    @JvmStatic
    fun importDataBaseMetaData(file: File, connectorRepository: ConnectorRepository = ConnectorRepository()): DatabaseMetaData {
      FileInputStream(file).use {
        val databaseMetaData = JSON.decodeFromStream<DatabaseMetaData>(it) as InternalDatabaseMetaData
        val columnMap = HashMap<UUID, ColumnMetaData>()

        databaseMetaData.connectorRepository = connectorRepository

        // Pass 1: Make sure all columns are updated
        databaseMetaData.tableMetaData.forEach { table ->
          (table as InternalTableMetaData).database = databaseMetaData

          table.columns.forEach { column ->
            updateColumn(column as InternalColumnMetaData, table)
            columnMap[column.syntheticId] = column
          }
        }

        // Pass 2: Now we safely may replace column references in indexes and foreign keys
        databaseMetaData.tableMetaData.forEach { table ->
          table.indexes.forEach { index -> updateIndex(index as InternalIndexMetaData, table, columnMap) }

          table.exportedForeignKeys.forEach { fk -> updateForeignKey(fk as InternalForeignKeyMetaData, table, columnMap) }

          table.importedForeignKeys.forEach { fk -> updateForeignKey(fk as InternalForeignKeyMetaData, table, columnMap) }
        }

        return databaseMetaData
      }
    }

    private fun updateColumn(column: InternalColumnMetaData, table: TableMetaData) {
      column.table = table
    }

    private fun updateIndex(index: InternalIndexMetaData, table: TableMetaData, columnMap: Map<UUID, ColumnMetaData>) {
      index.table = table
      val columns = ArrayList(index.columns)

      index.clearColumns()

      for (columnMetaData in columns) {
        val column = columnMap.lookupColumn(columnMetaData)

        index.addColumn(column)
      }
    }

    private fun updateForeignKey(
      fk: InternalForeignKeyMetaData, table: TableMetaData, columnMap: Map<UUID, ColumnMetaData>
    ) {
      fk.table = table

      val referencingColumns = ArrayList(fk.referencingColumns)
      val referencedColumns = ArrayList(fk.referencedColumns)

      assert(referencedColumns.size == referencedColumns.size) { "Referenced columns: " + referencedColumns.size + " vs. " + referencingColumns.size }

      fk.clearReferencedColumns()
      fk.clearReferencingColumns()

      for (i in (0 until referencedColumns.size)) {
        val referencingColumn = columnMap.lookupColumn(referencingColumns[i])
        val referencedColumn = columnMap.lookupColumn(referencedColumns[i])

        fk.addColumnTuple(referencingColumn, referencedColumn)
      }
    }

    private fun Map<UUID, ColumnMetaData>.lookupColumn(columnMetaData: ColumnMetaData): ColumnMetaData {
      val column = this[columnMetaData.syntheticId]!!

      assert(column !== columnMetaData && column.columnName == columnMetaData.columnName) { "Column $columnMetaData.columnName mismatch" }

      return column
    }
  }
}