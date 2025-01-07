package io.github.guttenbase.schema

import io.github.guttenbase.connector.GuttenBaseException
import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool.Companion.executeScriptWithRetry

/**
 * Create Custom DDL from existing schema and run it on target schema.
 *
 * &copy; 2012-2044 akquinet tech@spree
 */
@Suppress("MemberVisibilityCanBePrivate")
class CopySchemaTool(
  private val connectorRepository: ConnectorRepository,
  private val sourceConnectorId: String, private val targetConnectorId: String
) {
  fun createDDLScript(): List<String> {
    val schemaScriptCreatorTool = SchemaScriptCreatorTool(connectorRepository, sourceConnectorId, targetConnectorId)

    return listOf(
      schemaScriptCreatorTool.createTableStatements(),
      schemaScriptCreatorTool.createMultiColumnPrimaryKeyStatements(),
      schemaScriptCreatorTool.createForeignKeyStatements(),
      schemaScriptCreatorTool.createIndexStatements(),
      schemaScriptCreatorTool.createAutoincrementUpdateStatements()
    ).flatten()
  }

  @JvmOverloads
  fun copySchema(prepareTargetConnection: Boolean = true, retryFailed: Boolean = false) {
    val result = executeScriptWithRetry(
      connectorRepository, targetConnectorId,
      prepareTargetConnection, retryFailed, createDDLScript()
    )

    if (result.hasFailures()) {
      throw GuttenBaseException("Failed to copy schema: ${result.failedStatements()}")
    }
  }
}
