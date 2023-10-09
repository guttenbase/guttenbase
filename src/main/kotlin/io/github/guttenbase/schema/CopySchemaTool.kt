package io.github.guttenbase.schema

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import java.sql.SQLException

/**
 * Create Custom DDL from existing schema
 *
 *  &copy; 2012-2034 akquinet tech@spree
 *
 */
@Suppress("MemberVisibilityCanBePrivate")
class CopySchemaTool(private val connectorRepository: ConnectorRepository) {
  fun createDDLScript(sourceConnectorId: String, targetConnectorId: String): List<String> {
    val schemaScriptCreatorTool = SchemaScriptCreatorTool(connectorRepository, sourceConnectorId, targetConnectorId)

    return listOf(
      schemaScriptCreatorTool.createTableStatements(),
      schemaScriptCreatorTool.createForeignKeyStatements(),
      schemaScriptCreatorTool.createIndexStatements(),
      schemaScriptCreatorTool.createMultiColumnPrimaryKeyStatements(),
      schemaScriptCreatorTool.createAutoincrementUpdateStatements()
    ).flatten()
  }

  @Throws(SQLException::class)
  fun copySchema(sourceConnectorId: String, targetConnectorId: String) {
    val ddlScript = createDDLScript(sourceConnectorId, targetConnectorId)

    ScriptExecutorTool(connectorRepository).executeScript(targetConnectorId, lines = ddlScript)
  }
}
