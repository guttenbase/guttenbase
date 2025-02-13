package io.github.guttenbase.tools

import io.github.guttenbase.meta.DatabaseMetaData
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.serialization.JSON
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.encodeToStream
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalSerializationApi::class)
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