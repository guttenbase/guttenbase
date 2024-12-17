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
class CopySchemaTool(private val connectorRepository: ConnectorRepository,
                     private val sourceConnectorId: String, private val targetConnectorId: String) {
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

  @Throws(SQLException::class)
  fun copySchema() {
    val ddlScript = createDDLScript()

    ScriptExecutorTool(connectorRepository).executeScript(targetConnectorId, lines = ddlScript)
  }
}
