@file:OptIn(ExperimentalSerializationApi::class)

package io.github.guttenbase.tools

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.serialization.JSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
}

fun importDataBaseMetaData(file: File): DatabaseMetaData {
  FileInputStream(file).use {
    return JSON.decodeFromStream<DatabaseMetaData>(it)
  }
}