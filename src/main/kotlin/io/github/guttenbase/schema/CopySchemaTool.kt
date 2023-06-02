package io.github.guttenbase.schema

import io.github.guttenbase.repository.ConnectorRepository
import io.github.guttenbase.tools.ScriptExecutorTool
import java.sql.SQLException

/**
 * Create Custom DDL from existing schema
 *
 *  2012-2034 akquinet tech@spree
 *
 */
class CopySchemaTool(private val connectorRepository: ConnectorRepository) {
  fun createDDLScript(sourceConnectorId: String, targetConnectorId: String): List<String> {
    val schemaScriptCreatorTool = SchemaScriptCreatorTool(
      connectorRepository, sourceConnectorId,
      targetConnectorId
    )

    return listOf(
      schemaScriptCreatorTool.createTableStatements(),
      schemaScriptCreatorTool.createPrimaryKeyStatements(),
      schemaScriptCreatorTool.createForeignKeyStatements(), schemaScriptCreatorTool.createIndexStatements()
    ).flatten()
  }

  @Throws(SQLException::class)
  fun copySchema(sourceConnectorId: String, targetConnectorId: String) {
    val ddlScript = createDDLScript(sourceConnectorId, targetConnectorId)

    ScriptExecutorTool(connectorRepository).executeScript(targetConnectorId, lines = ddlScript)
  }
}
